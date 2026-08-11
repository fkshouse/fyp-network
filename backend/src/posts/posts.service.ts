import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';
import { NotificationsService } from '../notifications/notifications.service';
import { CreatePostDto } from './dto/create-post.dto';
import { CreateCommentDto } from './dto/create-comment.dto';
import { PaginateFeedDto } from './dto/paginate-feed.dto';

const POST_INCLUDE = {
  author: true,
  media: true,
  _count: { select: { comments: true, likes: true } },
} as const;

@Injectable()
export class PostsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
    private readonly notifications: NotificationsService,
  ) {}

  async createPost(authorId: string, dto: CreatePostDto, uploadedFiles: Express.Multer.File[]) {
    const mediaKeys = await Promise.all(
      uploadedFiles.map((file) => this.media.upload(file.buffer, file.mimetype, file.originalname)),
    );

    const post = await this.prisma.post.create({
      data: {
        authorId,
        content: dto.content,
        media: {
          create: mediaKeys.map((key, i) => ({
            objectKey: key,
            mimeType: uploadedFiles[i].mimetype,
          })),
        },
      },
      include: POST_INCLUDE,
    });

    return this.toFeedItem(post, authorId);
  }

  async getFeed(viewerId: string, query: PaginateFeedDto) {
    const posts = await this.prisma.post.findMany({
      take: query.limit ?? 20,
      where: query.authorId ? { authorId: query.authorId } : undefined,
      ...(query.cursor
        ? { skip: 1, cursor: { id: query.cursor } }
        : {}),
      orderBy: { createdAt: 'desc' },
      include: POST_INCLUDE,
    });

    // Figure out which of these posts the viewer has liked, in one query
    // rather than N+1-ing it per post.
    const likedPostIds = await this.likedPostIds(viewerId, posts.map((p) => p.id));

    return {
      items: posts.map((post) => this.toFeedItem(post, viewerId, likedPostIds)),
      nextCursor: posts.length > 0 ? posts[posts.length - 1].id : null,
    };
  }

  async getPost(postId: string, viewerId: string) {
    const post = await this.prisma.post.findUnique({
      where: { id: postId },
      include: POST_INCLUDE,
    });
    if (!post) {
      throw new NotFoundException('Post not found');
    }
    const likedPostIds = await this.likedPostIds(viewerId, [postId]);
    return this.toFeedItem(post, viewerId, likedPostIds);
  }

  async addComment(postId: string, authorId: string, dto: CreateCommentDto) {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) {
      throw new NotFoundException('Post not found');
    }

    const comment = await this.prisma.comment.create({
      data: { postId, authorId, content: dto.content },
      include: { author: true },
    });

    await this.notifications.create({
      userId: post.authorId,
      actorId: authorId,
      type: 'POST_COMMENT',
      message: `${comment.author.firstName} ${comment.author.lastName} commented on your post`,
      postId,
    });

    return this.toCommentDto(comment);
  }

  async listComments(postId: string) {
    await this.assertPostExists(postId);
    const comments = await this.prisma.comment.findMany({
      where: { postId },
      orderBy: { createdAt: 'asc' },
      include: { author: true },
    });
    return comments.map((c) => this.toCommentDto(c));
  }

  async toggleLike(postId: string, userId: string) {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) {
      throw new NotFoundException('Post not found');
    }
    const existing = await this.prisma.like.findUnique({
      where: { postId_userId: { postId, userId } },
    });

    if (existing) {
      await this.prisma.like.delete({ where: { id: existing.id } });
      return { liked: false };
    }

    await this.prisma.like.create({ data: { postId, userId } });

    const liker = await this.prisma.user.findUniqueOrThrow({ where: { id: userId } });
    await this.notifications.create({
      userId: post.authorId,
      actorId: userId,
      type: 'POST_LIKE',
      message: `${liker.firstName} ${liker.lastName} liked your post`,
      postId,
    });

    return { liked: true };
  }

  async updatePost(postId: string, userId: string, content: string) {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) {
      throw new NotFoundException('Post not found');
    }
    if (post.authorId !== userId) {
      throw new ForbiddenException('Only the author can edit this post');
    }

    const updated = await this.prisma.post.update({
      where: { id: postId },
      data: { content },
      include: POST_INCLUDE,
    });
    const likedPostIds = await this.likedPostIds(userId, [postId]);
    return this.toFeedItem(updated, userId, likedPostIds);
  }

  async deletePost(postId: string, userId: string) {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) {
      throw new NotFoundException('Post not found');
    }
    if (post.authorId !== userId) {
      throw new ForbiddenException('Only the author can delete this post');
    }
    await this.prisma.post.delete({ where: { id: postId } });
  }

  private async assertPostExists(postId: string) {
    const exists = await this.prisma.post.findUnique({ where: { id: postId }, select: { id: true } });
    if (!exists) {
      throw new NotFoundException('Post not found');
    }
  }

  private async likedPostIds(userId: string, postIds: string[]): Promise<Set<string>> {
    if (postIds.length === 0) return new Set();
    const likes = await this.prisma.like.findMany({
      where: { userId, postId: { in: postIds } },
      select: { postId: true },
    });
    return new Set(likes.map((l) => l.postId));
  }

  private toFeedItem(
    post: {
      id: string;
      content: string;
      createdAt: Date;
      author: { id: string; firstName: string; lastName: string; profilePicture: string | null };
      media: { objectKey: string; mimeType: string }[];
      _count: { comments: number; likes: number };
    },
    _viewerId: string,
    likedPostIds?: Set<string>,
  ) {
    return {
      id: post.id,
      content: post.content,
      createdAt: post.createdAt,
      author: {
        id: post.author.id,
        name: `${post.author.firstName} ${post.author.lastName}`,
        profilePictureUrl: post.author.profilePicture
          ? this.media.resolveUrl(post.author.profilePicture)
          : null,
      },
      media: post.media.map((m) => ({
        url: this.media.resolveUrl(m.objectKey),
        mimeType: m.mimeType,
      })),
      commentCount: post._count.comments,
      likeCount: post._count.likes,
      likedByViewer: likedPostIds?.has(post.id) ?? false,
    };
  }

  private toCommentDto(comment: {
    id: string;
    content: string;
    createdAt: Date;
    author: { id: string; firstName: string; lastName: string; profilePicture: string | null };
  }) {
    return {
      id: comment.id,
      content: comment.content,
      createdAt: comment.createdAt,
      author: {
        id: comment.author.id,
        name: `${comment.author.firstName} ${comment.author.lastName}`,
        profilePictureUrl: comment.author.profilePicture
          ? this.media.resolveUrl(comment.author.profilePicture)
          : null,
      },
    };
  }
}
