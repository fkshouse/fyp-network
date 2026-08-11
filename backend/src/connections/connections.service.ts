import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConnectionStatus } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';
import { NotificationsService } from '../notifications/notifications.service';

@Injectable()
export class ConnectionsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
    private readonly notifications: NotificationsService,
  ) {}

  async sendRequest(requesterId: string, addresseeId: string) {
    if (requesterId === addresseeId) {
      throw new BadRequestException("You can't connect with yourself");
    }

    // A request may already exist in either direction - treat that as a conflict
    // rather than silently creating a duplicate/reverse row.
    const existing = await this.prisma.connection.findFirst({
      where: {
        OR: [
          { requesterId, addresseeId },
          { requesterId: addresseeId, addresseeId: requesterId },
        ],
      },
    });
    if (existing) {
      throw new ConflictException('A connection or pending request already exists');
    }

    const connection = await this.prisma.connection.create({
      data: { requesterId, addresseeId, status: ConnectionStatus.PENDING },
    });

    const requester = await this.prisma.user.findUniqueOrThrow({ where: { id: requesterId } });
    await this.notifications.create({
      userId: addresseeId,
      actorId: requesterId,
      type: 'CONNECTION_REQUEST',
      message: `${requester.firstName} ${requester.lastName} sent you a connection request`,
      connectionId: connection.id,
    });

    return this.toDto(connection.id);
  }

  async respond(userId: string, connectionId: string, accept: boolean) {
    const connection = await this.prisma.connection.findUnique({ where: { id: connectionId } });
    if (!connection) {
      throw new NotFoundException('Connection request not found');
    }
    if (connection.addresseeId !== userId) {
      throw new ForbiddenException('Only the recipient can respond to this request');
    }
    if (connection.status !== ConnectionStatus.PENDING) {
      throw new BadRequestException('This request has already been handled');
    }

    const updated = await this.prisma.connection.update({
      where: { id: connectionId },
      data: { status: accept ? ConnectionStatus.ACCEPTED : ConnectionStatus.DECLINED },
    });

    if (accept) {
      const addressee = await this.prisma.user.findUniqueOrThrow({ where: { id: userId } });
      await this.notifications.create({
        userId: connection.requesterId,
        actorId: userId,
        type: 'CONNECTION_ACCEPTED',
        message: `${addressee.firstName} ${addressee.lastName} accepted your connection request`,
        connectionId: updated.id,
      });
    }

    return this.toDto(updated.id);
  }

  async remove(userId: string, connectionId: string) {
    const connection = await this.prisma.connection.findUnique({ where: { id: connectionId } });
    if (!connection) {
      throw new NotFoundException('Connection not found');
    }
    if (connection.requesterId !== userId && connection.addresseeId !== userId) {
      throw new ForbiddenException("You're not part of this connection");
    }
    await this.prisma.connection.delete({ where: { id: connectionId } });
  }

  async listAccepted(userId: string) {
    const connections = await this.prisma.connection.findMany({
      where: {
        status: ConnectionStatus.ACCEPTED,
        OR: [{ requesterId: userId }, { addresseeId: userId }],
      },
      include: { requester: true, addressee: true },
      orderBy: { updatedAt: 'desc' },
    });

    return connections.map((c) => {
      const other = c.requesterId === userId ? c.addressee : c.requester;
      return {
        connectionId: c.id,
        user: {
          id: other.id,
          name: `${other.firstName} ${other.lastName}`,
          headline: other.headline,
          profilePictureUrl: other.profilePicture ? this.media.resolveUrl(other.profilePicture) : null,
        },
      };
    });
  }

  async listPendingReceived(userId: string) {
    const connections = await this.prisma.connection.findMany({
      where: { addresseeId: userId, status: ConnectionStatus.PENDING },
      include: { requester: true },
      orderBy: { createdAt: 'desc' },
    });

    return connections.map((c) => ({
      connectionId: c.id,
      user: {
        id: c.requester.id,
        name: `${c.requester.firstName} ${c.requester.lastName}`,
        headline: c.requester.headline,
        profilePictureUrl: c.requester.profilePicture
          ? this.media.resolveUrl(c.requester.profilePicture)
          : null,
      },
      createdAt: c.createdAt,
    }));
  }

  // What UserProfileScreen actually needs: not three separate lists to scan
  // through client-side, but a direct answer to "what's my relationship with
  // this specific person right now" - queried fresh from the DB each time,
  // so it's always correct even across app restarts or re-opening a profile.
  async getStatusWith(userId: string, otherUserId: string) {
    if (userId === otherUserId) {
      return { status: 'SELF' as const, connectionId: null };
    }

    const connection = await this.prisma.connection.findFirst({
      where: {
        OR: [
          { requesterId: userId, addresseeId: otherUserId },
          { requesterId: otherUserId, addresseeId: userId },
        ],
      },
    });

    if (!connection) {
      return { status: 'NONE' as const, connectionId: null };
    }
    if (connection.status === ConnectionStatus.ACCEPTED) {
      return { status: 'CONNECTED' as const, connectionId: connection.id };
    }
    if (connection.status === ConnectionStatus.PENDING) {
      const status = connection.requesterId === userId ? ('PENDING_SENT' as const) : ('PENDING_RECEIVED' as const);
      return { status, connectionId: connection.id };
    }
    // DECLINED - treat as if nothing exists, so the person can send a fresh request.
    return { status: 'NONE' as const, connectionId: null };
  }

  private async toDto(connectionId: string) {
    const c = await this.prisma.connection.findUniqueOrThrow({ where: { id: connectionId } });
    return { id: c.id, status: c.status, requesterId: c.requesterId, addresseeId: c.addresseeId };
  }
}
