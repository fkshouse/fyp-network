import { Injectable, NotFoundException } from '@nestjs/common';
import { ConnectionStatus } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateUserDto } from './dto/update-user.dto';
import { MediaService } from '../media/media.service';

@Injectable()
export class UsersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly media: MediaService,
  ) {}

  async findPublicProfile(userId: string) {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return this.toPublicProfile(user);
  }

  async update(userId: string, dto: UpdateUserDto) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: dto,
    });
    return this.toPublicProfile(user);
  }

  async search(query: string, excludeUserId: string) {
    if (!query || query.trim().length < 2) return [];

    const users = await this.prisma.user.findMany({
      where: {
        id: { not: excludeUserId },
        OR: [
          { firstName: { contains: query, mode: 'insensitive' } },
          { lastName: { contains: query, mode: 'insensitive' } },
          { email: { contains: query, mode: 'insensitive' } },
        ],
      },
      take: 20,
    });

    // One query for every result's connection status, rather than N+1 -
    // find every connection row between the searching user and anyone in
    // this result set, in either direction.
    const foundIds = users.map((u) => u.id);
    const connections = await this.prisma.connection.findMany({
      where: {
        OR: [
          { requesterId: excludeUserId, addresseeId: { in: foundIds } },
          { requesterId: { in: foundIds }, addresseeId: excludeUserId },
        ],
      },
    });

    return users.map((u) => {
      const connection = connections.find(
        (c) =>
          (c.requesterId === excludeUserId && c.addresseeId === u.id) ||
          (c.requesterId === u.id && c.addresseeId === excludeUserId),
      );

      let connectionStatus: 'NONE' | 'PENDING_SENT' | 'PENDING_RECEIVED' | 'CONNECTED' = 'NONE';
      if (connection?.status === ConnectionStatus.ACCEPTED) {
        connectionStatus = 'CONNECTED';
      } else if (connection?.status === ConnectionStatus.PENDING) {
        connectionStatus = connection.requesterId === excludeUserId ? 'PENDING_SENT' : 'PENDING_RECEIVED';
      }

      return {
        ...this.toPublicProfile(u),
        connectionStatus,
        connectionId: connection && connection.status !== ConnectionStatus.DECLINED ? connection.id : null,
      };
    });
  }

  async setProfilePicture(userId: string, objectKey: string) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { profilePicture: objectKey },
    });
    return this.toPublicProfile(user);
  }

  private toPublicProfile(user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    headline: string | null;
    company: string | null;
    bio: string | null;
    profilePicture: string | null;
    createdAt: Date;
  }) {
    return {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      headline: user.headline,
      company: user.company,
      bio: user.bio,
      profilePictureUrl: user.profilePicture
        ? this.media.resolveUrl(user.profilePicture)
        : null,
      memberSince: user.createdAt,
    };
  }
}
