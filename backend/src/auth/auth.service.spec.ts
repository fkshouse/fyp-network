import { Test } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { ConflictException, UnauthorizedException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { AuthService } from './auth.service';
import { PrismaService } from '../prisma/prisma.service';

describe('AuthService', () => {
  let authService: AuthService;
  let prisma: { user: any; refreshToken: any };

  beforeEach(async () => {
    prisma = {
      user: {
        findUnique: jest.fn(),
        findUniqueOrThrow: jest.fn(),
        create: jest.fn(),
      },
      refreshToken: {
        create: jest.fn(),
        findUnique: jest.fn(),
        update: jest.fn(),
      },
    };

    const moduleRef = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: PrismaService, useValue: prisma },
        {
          provide: JwtService,
          useValue: {
            sign: jest.fn().mockReturnValue('signed.jwt.token'),
            verify: jest.fn(),
          },
        },
        {
          provide: ConfigService,
          useValue: {
            get: jest.fn((key: string) => {
              const values: Record<string, string> = {
                JWT_ACCESS_SECRET: 'test-access-secret',
                JWT_ACCESS_EXPIRES_IN: '15m',
                JWT_REFRESH_SECRET: 'test-refresh-secret',
                JWT_REFRESH_EXPIRES_IN: '30d',
              };
              return values[key];
            }),
          },
        },
      ],
    }).compile();

    authService = moduleRef.get(AuthService);
  });

  describe('register', () => {
    it('rejects a duplicate email', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'existing-user' });

      await expect(
        authService.register({
          email: 'taken@example.com',
          password: 'password123',
          firstName: 'A',
          lastName: 'B',
        }),
      ).rejects.toBeInstanceOf(ConflictException);
    });

    it('hashes the password with bcrypt before storing it, never the raw password', async () => {
      prisma.user.findUnique.mockResolvedValue(null);
      prisma.user.create.mockImplementation(({ data }: any) =>
        Promise.resolve({ id: 'new-user', ...data }),
      );

      await authService.register({
        email: 'new@example.com',
        password: 'plaintext-password',
        firstName: 'Jane',
        lastName: 'Doe',
      });

      const createCallArg = prisma.user.create.mock.calls[0][0];
      expect(createCallArg.data.passwordHash).not.toBe('plaintext-password');
      const matches = await bcrypt.compare('plaintext-password', createCallArg.data.passwordHash);
      expect(matches).toBe(true);
    });
  });

  describe('login', () => {
    it('throws the same error for a missing user and a wrong password, to avoid leaking which one it was', async () => {
      prisma.user.findUnique.mockResolvedValue(null);
      await expect(authService.login({ email: 'nobody@example.com', password: 'x' })).rejects.toBeInstanceOf(
        UnauthorizedException,
      );

      const hash = await bcrypt.hash('correct-password', 12);
      prisma.user.findUnique.mockResolvedValue({ id: 'u1', email: 'a@b.com', passwordHash: hash });
      await expect(
        authService.login({ email: 'a@b.com', password: 'wrong-password' }),
      ).rejects.toBeInstanceOf(UnauthorizedException);
    });

    it('succeeds and issues a token pair for a correct password', async () => {
      const hash = await bcrypt.hash('correct-password', 12);
      prisma.user.findUnique.mockResolvedValue({
        id: 'u1',
        email: 'a@b.com',
        firstName: 'A',
        lastName: 'B',
        passwordHash: hash,
      });
      prisma.refreshToken.create.mockResolvedValue({});

      const result = await authService.login({ email: 'a@b.com', password: 'correct-password' });

      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBeDefined();
      expect(result.user.email).toBe('a@b.com');
    });
  });
});
