import { Test } from '@nestjs/testing';
import { BadRequestException, ConflictException, ForbiddenException } from '@nestjs/common';
import { ConnectionsService } from './connections.service';
import { PrismaService } from '../prisma/prisma.service';
import { MediaService } from '../media/media.service';
import { NotificationsService } from '../notifications/notifications.service';

describe('ConnectionsService', () => {
  let service: ConnectionsService;
  let prisma: { connection: any; user: any };
  let notifications: { create: jest.Mock };

  beforeEach(async () => {
    prisma = {
      connection: {
        findFirst: jest.fn(),
        findUnique: jest.fn(),
        findUniqueOrThrow: jest.fn(),
        create: jest.fn(),
        update: jest.fn(),
        delete: jest.fn(),
        findMany: jest.fn(),
      },
      user: {
        findUniqueOrThrow: jest.fn().mockResolvedValue({ firstName: 'Jane', lastName: 'Doe' }),
      },
    };
    notifications = { create: jest.fn() };

    const moduleRef = await Test.createTestingModule({
      providers: [
        ConnectionsService,
        { provide: PrismaService, useValue: prisma },
        { provide: MediaService, useValue: { resolveUrl: jest.fn((k: string) => `http://media/${k}`) } },
        { provide: NotificationsService, useValue: notifications },
      ],
    }).compile();

    service = moduleRef.get(ConnectionsService);
  });

  it('rejects connecting with yourself', async () => {
    await expect(service.sendRequest('user-1', 'user-1')).rejects.toBeInstanceOf(BadRequestException);
  });

  it('rejects a duplicate request in either direction', async () => {
    prisma.connection.findFirst.mockResolvedValue({ id: 'existing' });

    await expect(service.sendRequest('user-1', 'user-2')).rejects.toBeInstanceOf(ConflictException);
  });

  it('creates a pending request and notifies the addressee', async () => {
    prisma.connection.findFirst.mockResolvedValue(null);
    prisma.connection.create.mockResolvedValue({ id: 'conn-1' });
    prisma.connection.findUniqueOrThrow.mockResolvedValue({
      id: 'conn-1',
      status: 'PENDING',
      requesterId: 'user-1',
      addresseeId: 'user-2',
    });

    const result = await service.sendRequest('user-1', 'user-2');

    expect(result.status).toBe('PENDING');
    expect(notifications.create).toHaveBeenCalledWith(
      expect.objectContaining({ userId: 'user-2', type: 'CONNECTION_REQUEST' }),
    );
  });

  it('only lets the addressee accept or decline a request', async () => {
    prisma.connection.findUnique.mockResolvedValue({
      id: 'conn-1',
      status: 'PENDING',
      requesterId: 'user-1',
      addresseeId: 'user-2',
    });

    await expect(service.respond('user-1', 'conn-1', true)).rejects.toBeInstanceOf(ForbiddenException);
  });

  it('rejects responding to an already-handled request', async () => {
    prisma.connection.findUnique.mockResolvedValue({
      id: 'conn-1',
      status: 'ACCEPTED',
      requesterId: 'user-1',
      addresseeId: 'user-2',
    });

    await expect(service.respond('user-2', 'conn-1', true)).rejects.toBeInstanceOf(BadRequestException);
  });
});
