import { Module } from '@nestjs/common';
import { TasksService } from './tasks.service';
import { TasksController } from './tasks.controller';
import { GroupTasksController } from './group-tasks.controller';
import { MediaModule } from '../media/media.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { GroupsModule } from '../groups/groups.module';

@Module({
  imports: [MediaModule, NotificationsModule, GroupsModule],
  providers: [TasksService],
  controllers: [TasksController, GroupTasksController],
})
export class TasksModule {}
