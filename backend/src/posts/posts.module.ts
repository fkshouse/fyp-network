import { Module } from '@nestjs/common';
import { PostsService } from './posts.service';
import { PostsController } from './posts.controller';
import { MediaModule } from '../media/media.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [MediaModule, NotificationsModule],
  providers: [PostsService],
  controllers: [PostsController],
})
export class PostsModule {}
