import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateUserDto {
  @ApiPropertyOptional({ example: 'Software Engineer at Acme' })
  @IsOptional()
  @IsString()
  @MaxLength(120)
  headline?: string;

  @ApiPropertyOptional({ example: 'Acme Corp' })
  @IsOptional()
  @IsString()
  @MaxLength(120)
  company?: string;

  @ApiPropertyOptional({ example: 'Backend-leaning full-stack engineer.' })
  @IsOptional()
  @IsString()
  @MaxLength(2000)
  bio?: string;
}
