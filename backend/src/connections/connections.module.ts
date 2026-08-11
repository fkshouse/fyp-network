import { Module } from '@nestjs/common';
import { ConnectionsService } from './connections.service';
import { ConnectionsController } from './connections.controller';
import { MediaModule } from '../media/media.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [MediaModule, NotificationsModule],
  providers: [ConnectionsService],
  controllers: [ConnectionsController],
})
export class ConnectionsModule {}
