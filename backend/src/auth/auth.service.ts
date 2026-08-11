import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { randomUUID } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';

const BCRYPT_ROUNDS = 12;

interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwt: JwtService,
    private readonly config: ConfigService,
  ) {}

  async register(dto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({ where: { email: dto.email } });
    if (existing) {
      throw new ConflictException('An account with this email already exists');
    }

    // Passwords are hashed with bcrypt (salted, adaptive cost) - never
    // stored reversibly, unlike the original app's sha1(password+username).
    const passwordHash = await bcrypt.hash(dto.password, BCRYPT_ROUNDS);

    const user = await this.prisma.user.create({
      data: {
        email: dto.email,
        passwordHash,
        firstName: dto.firstName,
        lastName: dto.lastName,
      },
    });

    // Deliberately does NOT log the user in automatically - registration and
    // authentication are separate actions, and auto-login here would also
    // mean a typo'd password on signup silently locks you into an account
    // you can't get back into without knowing what you originally typed.
    return { user: this.toPublicUser(user) };
  }

  async login(dto: LoginDto) {
    const user = await this.prisma.user.findUnique({ where: { email: dto.email } });
    // Same error for "no such user" and "wrong password" - don't leak
    // which one it was, that's an account-enumeration vector.
    if (!user) {
      throw new UnauthorizedException('Invalid email or password');
    }

    const passwordMatches = await bcrypt.compare(dto.password, user.passwordHash);
    if (!passwordMatches) {
      throw new UnauthorizedException('Invalid email or password');
    }

    const tokens = await this.issueTokenPair(user.id, user.email);
    return { user: this.toPublicUser(user), ...tokens };
  }

  async refresh(rawRefreshToken: string) {
    let payload: { sub: string; jti: string };
    try {
      payload = this.jwt.verify(rawRefreshToken, {
        secret: this.config.get<string>('JWT_REFRESH_SECRET'),
      });
    } catch {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }

    const stored = await this.prisma.refreshToken.findUnique({ where: { id: payload.jti } });
    if (!stored || stored.revokedAt || stored.expiresAt < new Date()) {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }

    const matches = await bcrypt.compare(rawRefreshToken, stored.tokenHash);
    if (!matches) {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }

    // Rotate: revoke the used token and issue a brand new pair. This means
    // a stolen-and-replayed refresh token is only usable once.
    await this.prisma.refreshToken.update({
      where: { id: stored.id },
      data: { revokedAt: new Date() },
    });

    const user = await this.prisma.user.findUniqueOrThrow({ where: { id: stored.userId } });
    return this.issueTokenPair(user.id, user.email);
  }

  async changePassword(userId: string, currentPassword: string, newPassword: string) {
    const user = await this.prisma.user.findUniqueOrThrow({ where: { id: userId } });

    const matches = await bcrypt.compare(currentPassword, user.passwordHash);
    if (!matches) {
      throw new UnauthorizedException('Current password is incorrect');
    }

    const passwordHash = await bcrypt.hash(newPassword, BCRYPT_ROUNDS);
    await this.prisma.user.update({ where: { id: userId }, data: { passwordHash } });

    // Changing your password invalidates every existing refresh token - if
    // someone else had a stolen session going, this kicks them out too.
    await this.prisma.refreshToken.updateMany({
      where: { userId, revokedAt: null },
      data: { revokedAt: new Date() },
    });
  }

  async logout(refreshTokenId: string) {
    await this.prisma.refreshToken.updateMany({
      where: { id: refreshTokenId, revokedAt: null },
      data: { revokedAt: new Date() },
    });
  }

  private async issueTokenPair(userId: string, email: string): Promise<TokenPair> {
    const accessToken = this.jwt.sign(
      { sub: userId, email },
      {
        secret: this.config.get<string>('JWT_ACCESS_SECRET'),
        expiresIn: this.config.get<string>('JWT_ACCESS_EXPIRES_IN'),
      },
    );

    const jti = randomUUID();
    const refreshExpiresIn = this.config.get<string>('JWT_REFRESH_EXPIRES_IN') ?? '30d';
    const refreshToken = this.jwt.sign(
      { sub: userId, jti },
      {
        secret: this.config.get<string>('JWT_REFRESH_SECRET'),
        expiresIn: refreshExpiresIn,
      },
    );

    const refreshTokenHash = await bcrypt.hash(refreshToken, BCRYPT_ROUNDS);
    await this.prisma.refreshToken.create({
      data: {
        id: jti,
        userId,
        tokenHash: refreshTokenHash,
        expiresAt: addDuration(new Date(), refreshExpiresIn),
      },
    });

    return { accessToken, refreshToken };
  }

  private toPublicUser(user: { id: string; email: string; firstName: string; lastName: string }) {
    return {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
    };
  }
}

// Minimal "30d" / "15m" / "12h" duration parser so we can compute an
// expiresAt Date without pulling in a whole date library for one thing.
function addDuration(base: Date, spec: string): Date {
  const match = /^(\d+)([smhd])$/.exec(spec.trim());
  if (!match) {
    // fall back to 30 days if the env var is malformed
    return new Date(base.getTime() + 30 * 24 * 60 * 60 * 1000);
  }
  const value = Number(match[1]);
  const unitMs: Record<string, number> = {
    s: 1000,
    m: 60 * 1000,
    h: 60 * 60 * 1000,
    d: 24 * 60 * 60 * 1000,
  };
  return new Date(base.getTime() + value * unitMs[match[2]]);
}
