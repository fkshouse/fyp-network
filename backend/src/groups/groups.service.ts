import { ConflictException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { MembershipRole } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';
import { NotificationsService } from '../notifications/notifications.service';
import { CreateGroupDto } from './dto/create-group.dto';
import { UpdateGroupDto } from './dto/update-group.dto';

@Injectable()
export class GroupsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
    private readonly notifications: NotificationsService,
  ) {}

  async create(userId: string, dto: CreateGroupDto) {
    // De-dupe and drop the creator from the initial member list - they're
    // added separately as OWNER below, adding them twice would violate the
    // @@unique([groupId, userId]) constraint on Membership.
    const initialMemberIds = [...new Set(dto.memberIds ?? [])].filter((id) => id !== userId);

    const group = await this.prisma.group.create({
      data: {
        name: dto.name,
        description: dto.description,
        createdById: userId,
        members: {
          create: [
            { userId, role: MembershipRole.OWNER },
            ...initialMemberIds.map((id) => ({ userId: id, role: MembershipRole.MEMBER })),
          ],
        },
      },
    });

    if (initialMemberIds.length > 0) {
      const creator = await this.prisma.user.findUniqueOrThrow({ where: { id: userId } });
      await Promise.all(
        initialMemberIds.map((memberId) =>
          this.notifications.create({
            userId: memberId,
            actorId: userId,
            type: 'GROUP_ADDED',
            message: `${creator.firstName} ${creator.lastName} added you to the group "${group.name}"`,
            groupId: group.id,
          }),
        ),
      );
    }

    return this.getDetail(group.id, userId);
  }

  async listMine(userId: string) {
    const memberships = await this.prisma.membership.findMany({
      where: { userId },
      include: { group: { include: { _count: { select: { members: true, tasks: true } } } } },
      orderBy: { joinedAt: 'desc' },
    });

    return memberships.map((m) => ({
      id: m.group.id,
      name: m.group.name,
      description: m.group.description,
      role: m.role,
      memberCount: m.group._count.members,
      taskCount: m.group._count.tasks,
    }));
  }

  async getDetail(groupId: string, userId: string) {
    await this.assertMember(groupId, userId);

    const group = await this.prisma.group.findUniqueOrThrow({
      where: { id: groupId },
      include: { members: { include: { user: true } } },
    });

    return {
      id: group.id,
      name: group.name,
      description: group.description,
      createdAt: group.createdAt,
      members: group.members.map((m) => ({
        userId: m.user.id,
        name: `${m.user.firstName} ${m.user.lastName}`,
        role: m.role,
        profilePictureUrl: m.user.profilePicture ? this.media.resolveUrl(m.user.profilePicture) : null,
      })),
    };
  }

  async addMember(groupId: string, actingUserId: string, newUserId: string) {
    await this.assertAdmin(groupId, actingUserId);

    const existing = await this.prisma.membership.findUnique({
      where: { groupId_userId: { groupId, userId: newUserId } },
    });
    if (existing) {
      throw new ConflictException('That user is already a member of this group');
    }

    const group = await this.prisma.group.findUniqueOrThrow({ where: { id: groupId } });
    await this.prisma.membership.create({
      data: { groupId, userId: newUserId, role: MembershipRole.MEMBER },
    });

    const actor = await this.prisma.user.findUniqueOrThrow({ where: { id: actingUserId } });
    await this.notifications.create({
      userId: newUserId,
      actorId: actingUserId,
      type: 'GROUP_ADDED',
      message: `${actor.firstName} ${actor.lastName} added you to the group "${group.name}"`,
      groupId: group.id,
    });

    return this.getDetail(groupId, actingUserId);
  }

  async removeMember(groupId: string, actingUserId: string, targetUserId: string) {
    // Members can remove themselves (leave); otherwise you need admin rights.
    if (actingUserId !== targetUserId) {
      await this.assertAdmin(groupId, actingUserId);
    }

    const membership = await this.prisma.membership.findUnique({
      where: { groupId_userId: { groupId, userId: targetUserId } },
    });
    if (!membership) {
      throw new NotFoundException('That user is not a member of this group');
    }
    if (membership.role === MembershipRole.OWNER) {
      throw new ForbiddenException("The group owner can't be removed");
    }

    await this.prisma.membership.delete({ where: { id: membership.id } });
  }

  async update(groupId: string, userId: string, dto: UpdateGroupDto) {
    await this.assertAdmin(groupId, userId);
    await this.prisma.group.update({
      where: { id: groupId },
      data: { name: dto.name, description: dto.description },
    });
    return this.getDetail(groupId, userId);
  }

  async delete(groupId: string, userId: string) {
    const membership = await this.assertMember(groupId, userId);
    if (membership.role !== MembershipRole.OWNER) {
      throw new ForbiddenException('Only the group owner can delete this group');
    }
    // Cascades to memberships and tasks via the schema's onDelete: Cascade.
    await this.prisma.group.delete({ where: { id: groupId } });
  }

  async assertMember(groupId: string, userId: string) {
    const membership = await this.prisma.membership.findUnique({
      where: { groupId_userId: { groupId, userId } },
    });
    if (!membership) {
      throw new ForbiddenException("You're not a member of this group");
    }
    return membership;
  }

  private async assertAdmin(groupId: string, userId: string) {
    const membership = await this.assertMember(groupId, userId);
    if (membership.role === MembershipRole.MEMBER) {
      throw new ForbiddenException('Only group admins/owners can do this');
    }
    return membership;
  }
}
