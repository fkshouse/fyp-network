import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  UploadedFiles,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FilesInterceptor } from '@nestjs/platform-express';
import { ApiBearerAuth, ApiConsumes, ApiTags } from '@nestjs/swagger';
import { PostsService } from './posts.service';
import { CreatePostDto } from './dto/create-post.dto';
import { UpdatePostDto } from './dto/update-post.dto';
import { CreateCommentDto } from './dto/create-comment.dto';
import { PaginateFeedDto } from './dto/paginate-feed.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser, AuthenticatedUser } from '../common/decorators/current-user.decorator';

const MAX_MEDIA_PER_POST = 4;
const MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15MB

@ApiTags('posts')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('posts')
export class PostsController {
  constructor(private readonly postsService: PostsService) {}

  @Get()
  getFeed(@CurrentUser() user: AuthenticatedUser, @Query() query: PaginateFeedDto) {
    return this.postsService.getFeed(user.userId, query);
  }

  @Get(':id')
  getPost(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.postsService.getPost(id, user.userId);
  }

  @Post()
  @ApiConsumes('multipart/form-data')
  @UseInterceptors(
    FilesInterceptor('media', MAX_MEDIA_PER_POST, {
      limits: { fileSize: MAX_FILE_SIZE_BYTES },
    }),
  )
  createPost(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: CreatePostDto,
    @UploadedFiles() files: Express.Multer.File[] = [],
  ) {
    return this.postsService.createPost(user.userId, dto, files);
  }

  @Get(':id/comments')
  listComments(@Param('id') id: string) {
    return this.postsService.listComments(id);
  }

  @Patch(':id')
  updatePost(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id') id: string,
    @Body() dto: UpdatePostDto,
  ) {
    return this.postsService.updatePost(id, user.userId, dto.content);
  }

  @Delete(':id')
  deletePost(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.postsService.deletePost(id, user.userId);
  }

  @Post(':id/comments')
  addComment(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id') id: string,
    @Body() dto: CreateCommentDto,
  ) {
    return this.postsService.addComment(id, user.userId, dto);
  }

  @Post(':id/like')
  toggleLike(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.postsService.toggleLike(id, user.userId);
  }
}
