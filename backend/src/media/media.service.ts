import { Injectable, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import {
  S3Client,
  PutObjectCommand,
  CreateBucketCommand,
  HeadBucketCommand,
  PutBucketPolicyCommand,
} from '@aws-sdk/client-s3';
import { randomUUID } from 'crypto';

@Injectable()
export class MediaService implements OnModuleInit {
  private readonly client: S3Client;
  private readonly bucket: string;
  private readonly endpoint: string;
  private readonly publicEndpoint: string;

  constructor(private readonly config: ConfigService) {
    this.bucket = this.config.get<string>('S3_BUCKET')!;
    this.endpoint = this.config.get<string>('S3_ENDPOINT')!;
    // Falls back to S3_ENDPOINT if S3_PUBLIC_ENDPOINT isn't set, so this
    // doesn't break for anyone testing purely via curl/Swagger on the same
    // machine - it only matters once a client (like the Android emulator)
    // is a different network context than the backend itself.
    this.publicEndpoint = this.config.get<string>('S3_PUBLIC_ENDPOINT') ?? this.endpoint;

    this.client = new S3Client({
      endpoint: this.endpoint,
      region: this.config.get<string>('S3_REGION'),
      forcePathStyle: this.config.get<string>('S3_FORCE_PATH_STYLE') === 'true',
      credentials: {
        accessKeyId: this.config.get<string>('S3_ACCESS_KEY')!,
        secretAccessKey: this.config.get<string>('S3_SECRET_KEY')!,
      },
    });
  }

  // Auto-create the local bucket on boot so `docker compose up` + `npm run start:dev`
  // is enough to get a working dev environment with zero manual MinIO setup.
  async onModuleInit() {
    try {
      await this.client.send(new HeadBucketCommand({ Bucket: this.bucket }));
    } catch {
      await this.client.send(new CreateBucketCommand({ Bucket: this.bucket }));
    }

    // Deliberately outside the try/catch above: this needs to run every boot,
    // not just on first creation. If it only ran inside the "bucket didn't
    // exist yet" branch, any bucket created by an earlier run (before this
    // policy existed, or reset for any reason) would stay private forever -
    // which is exactly what was causing images to silently fail to load.
    // Local dev only: allow public GET so the Android app can load images
    // directly by URL without presigning. Do NOT do this for a real bucket.
    await this.client.send(
      new PutBucketPolicyCommand({
        Bucket: this.bucket,
        Policy: JSON.stringify({
          Version: '2012-10-17',
          Statement: [
            {
              Effect: 'Allow',
              Principal: '*',
              Action: ['s3:GetObject'],
              Resource: [`arn:aws:s3:::${this.bucket}/*`],
            },
          ],
        }),
      }),
    );
  }

  async upload(buffer: Buffer, mimeType: string, originalName: string): Promise<string> {
    const extension = originalName.includes('.') ? originalName.split('.').pop() : 'bin';
    const objectKey = `${randomUUID()}.${extension}`;

    await this.client.send(
      new PutObjectCommand({
        Bucket: this.bucket,
        Key: objectKey,
        Body: buffer,
        ContentType: mimeType,
      }),
    );

    return objectKey;
  }

  // For local dev, a plain path-style URL is enough since the bucket is public
  // on the local MinIO instance. In a real deployment this would return a
  // presigned URL or sit behind a CDN. Uses publicEndpoint, NOT endpoint -
  // this is the URL a client (e.g. the Android app) will actually fetch,
  // which is a different address than the one the backend itself uses.
  resolveUrl(objectKey: string): string {
    return `${this.publicEndpoint}/${this.bucket}/${objectKey}`;
  }
}
