import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';

export class SendConnectionRequestDto {
  @ApiProperty({ description: 'User id to connect with' })
  @IsUUID()
  addresseeId!: string;
}
