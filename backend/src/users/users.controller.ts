import {
  Body,
  Controller,
  Get,
  Param,
  Patch,
  Post,
  Query,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ApiBearerAuth, ApiConsumes, ApiTags } from '@nestjs/swagger';
import { UsersService } from './users.service';
import { UpdateUserDto } from './dto/update-user.dto';
import { MediaService } from '../media/media.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser, AuthenticatedUser } from '../common/decorators/current-user.decorator';

@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class UsersController {
  constructor(
    private readonly usersService: UsersService,
    private readonly media: MediaService,
  ) {}

  // Registered before the ':id' route below so "search" isn't swallowed as a param.
  @Get('search')
  search(@CurrentUser() user: AuthenticatedUser, @Query('q') query: string) {
    return this.usersService.search(query ?? '', user.userId);
  }

  @Patch('me')
  updateMe(@CurrentUser() user: AuthenticatedUser, @Body() dto: UpdateUserDto) {
    return this.usersService.update(user.userId, dto);
  }

  @Post('me/profile-picture')
  @ApiConsumes('multipart/form-data')
  @UseInterceptors(FileInterceptor('file'))
  async uploadProfilePicture(
    @CurrentUser() user: AuthenticatedUser,
    @UploadedFile() file: Express.Multer.File,
  ) {
    const objectKey = await this.media.upload(file.buffer, file.mimetype, file.originalname);
    return this.usersService.setProfilePicture(user.userId, objectKey);
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.usersService.findPublicProfile(id);
  }
}
