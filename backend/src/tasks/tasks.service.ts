import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';
import { NotificationsService } from '../notifications/notifications.service';
import { GroupsService } from '../groups/groups.service';
import { CreateTaskDto } from './dto/create-task.dto';
import { UpdateTaskDto } from './dto/update-task.dto';

@Injectable()
export class TasksService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
    private readonly notifications: NotificationsService,
    private readonly groups: GroupsService,
  ) {}

  async create(groupId: string, userId: string, dto: CreateTaskDto) {
    await this.groups.assertMember(groupId, userId);

    if (dto.assigneeId) {
      await this.groups.assertMember(groupId, dto.assigneeId);
    }

    const task = await this.prisma.task.create({
      data: {
        groupId,
        title: dto.title,
        description: dto.description,
        assigneeId: dto.assigneeId,
        dueDate: dto.dueDate ? new Date(dto.dueDate) : undefined,
        completionPercent: dto.completionPercent ?? 0,
        createdById: userId,
      },
      include: { assignee: true, createdBy: true },
    });

    if (dto.assigneeId && dto.assigneeId !== userId) {
      await this.notifications.create({
        userId: dto.assigneeId,
        actorId: userId,
        type: 'TASK_ASSIGNED',
        message: `${task.createdBy.firstName} ${task.createdBy.lastName} assigned you a task: "${task.title}"`,
        taskId: task.id,
        groupId: task.groupId,
      });
    }

    return this.toDto(task);
  }

  async listForGroup(groupId: string, userId: string) {
    await this.groups.assertMember(groupId, userId);

    const tasks = await this.prisma.task.findMany({
      where: { groupId },
      include: { assignee: true, createdBy: true },
      orderBy: { createdAt: 'desc' },
    });

    return tasks.map((t) => this.toDto(t));
  }

  async update(taskId: string, userId: string, dto: UpdateTaskDto) {
    const task = await this.prisma.task.findUnique({ where: { id: taskId } });
    if (!task) {
      throw new NotFoundException('Task not found');
    }
    await this.groups.assertMember(task.groupId, userId);

    if (dto.assigneeId) {
      await this.groups.assertMember(task.groupId, dto.assigneeId);
    }

    const updated = await this.prisma.task.update({
      where: { id: taskId },
      data: {
        title: dto.title,
        description: dto.description,
        status: dto.status,
        assigneeId: dto.assigneeId === null ? null : dto.assigneeId,
        dueDate: dto.dueDate ? new Date(dto.dueDate) : undefined,
        completionPercent: dto.completionPercent,
      },
      include: { assignee: true, createdBy: true },
    });

    if (dto.assigneeId && dto.assigneeId !== task.assigneeId && dto.assigneeId !== userId) {
      await this.notifications.create({
        userId: dto.assigneeId,
        actorId: userId,
        type: 'TASK_ASSIGNED',
        message: `You were assigned the task: "${updated.title}"`,
        taskId: updated.id,
        groupId: updated.groupId,
      });
    }

    return this.toDto(updated);
  }

  async delete(taskId: string, userId: string) {
    const task = await this.prisma.task.findUnique({ where: { id: taskId } });
    if (!task) {
      throw new NotFoundException('Task not found');
    }
    const membership = await this.groups.assertMember(task.groupId, userId);
    if (task.createdById !== userId && membership.role === 'MEMBER') {
      throw new ForbiddenException('Only the task creator or a group admin can delete this task');
    }
    await this.prisma.task.delete({ where: { id: taskId } });
  }

  private toDto(task: {
    id: string;
    groupId: string;
    title: string;
    description: string | null;
    status: string;
    completionPercent: number;
    dueDate: Date | null;
    createdAt: Date;
    assignee: { id: string; firstName: string; lastName: string; profilePicture: string | null } | null;
    createdBy: { id: string; firstName: string; lastName: string };
  }) {
    return {
      id: task.id,
      groupId: task.groupId,
      title: task.title,
      description: task.description,
      status: task.status,
      completionPercent: task.completionPercent,
      dueDate: task.dueDate,
      createdAt: task.createdAt,
      createdBy: { id: task.createdBy.id, name: `${task.createdBy.firstName} ${task.createdBy.lastName}` },
      assignee: task.assignee
        ? {
            id: task.assignee.id,
            name: `${task.assignee.firstName} ${task.assignee.lastName}`,
            profilePictureUrl: task.assignee.profilePicture
              ? this.media.resolveUrl(task.assignee.profilePicture)
              : null,
          }
        : null,
    };
  }
}
