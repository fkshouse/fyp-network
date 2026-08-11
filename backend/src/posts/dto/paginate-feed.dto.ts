import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class PaginateFeedDto {
  // Cursor-based pagination (the id of the last post seen) scales much
  // better than OFFSET/LIMIT once the feed has real volume.
  @ApiPropertyOptional({ description: 'Id of the last post seen, for cursor pagination' })
  @IsOptional()
  @IsString()
  cursor?: string;

  @ApiPropertyOptional({ description: 'Filter to posts by a single author - used by profile screens' })
  @IsOptional()
  @IsString()
  authorId?: string;

  @ApiPropertyOptional({ default: 20, minimum: 1, maximum: 50 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  limit?: number = 20;
}
