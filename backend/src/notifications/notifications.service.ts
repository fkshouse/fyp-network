import { Injectable } from '@nestjs/common';
import { NotificationType } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';

interface CreateNotificationInput {
  userId: string; // recipient
  actorId?: string;
  type: NotificationType;
  message: string;
  postId?: string;
  connectionId?: string;
  taskId?: string;
  groupId?: string;
}

@Injectable()
export class NotificationsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
  ) {}

  // Called from other services (connections, posts, tasks) as a side effect
  // of the action itself - never exposed directly as a public "create" route.
  async create(input: CreateNotificationInput) {
    // Don't notify someone about their own action (e.g. liking your own post).
    if (input.actorId === input.userId) return;

    await this.prisma.notification.create({
      data: {
        userId: input.userId,
        actorId: input.actorId,
        type: input.type,
        message: input.message,
        postId: input.postId,
        connectionId: input.connectionId,
        taskId: input.taskId,
        groupId: input.groupId,
      },
    });
  }

  async listForUser(userId: string) {
    const notifications = await this.prisma.notification.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50,
      include: { actor: true },
    });

    return notifications.map((n) => ({
      id: n.id,
      type: n.type,
      message: n.message,
      isRead: n.isRead,
      createdAt: n.createdAt,
      postId: n.postId,
      connectionId: n.connectionId,
      taskId: n.taskId,
      groupId: n.groupId,
      actor: n.actor
        ? {
            id: n.actor.id,
            name: `${n.actor.firstName} ${n.actor.lastName}`,
            profilePictureUrl: n.actor.profilePicture
              ? this.media.resolveUrl(n.actor.profilePicture)
              : null,
          }
        : null,
    }));
  }

  async unreadCount(userId: string): Promise<number> {
    return this.prisma.notification.count({ where: { userId, isRead: false } });
  }

  async markRead(userId: string, notificationId: string) {
    await this.prisma.notification.updateMany({
      where: { id: notificationId, userId },
      data: { isRead: true },
    });
  }

  async markAllRead(userId: string) {
    await this.prisma.notification.updateMany({
      where: { userId, isRead: false },
      data: { isRead: true },
    });
  }
}
