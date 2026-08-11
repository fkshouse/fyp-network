import { Body, Controller, Delete, Get, Param, Patch, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { ConnectionsService } from './connections.service';
import { SendConnectionRequestDto } from './dto/send-connection-request.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser, AuthenticatedUser } from '../common/decorators/current-user.decorator';

@ApiTags('connections')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('connections')
export class ConnectionsController {
  constructor(private readonly connectionsService: ConnectionsService) {}

  @Post()
  sendRequest(@CurrentUser() user: AuthenticatedUser, @Body() dto: SendConnectionRequestDto) {
    return this.connectionsService.sendRequest(user.userId, dto.addresseeId);
  }

  @Get()
  list(@CurrentUser() user: AuthenticatedUser, @Query('status') status?: 'pending' | 'accepted') {
    if (status === 'pending') {
      return this.connectionsService.listPendingReceived(user.userId);
    }
    return this.connectionsService.listAccepted(user.userId);
  }

  @Get('status/:userId')
  getStatus(@CurrentUser() user: AuthenticatedUser, @Param('userId') otherUserId: string) {
    return this.connectionsService.getStatusWith(user.userId, otherUserId);
  }

  @Patch(':id/accept')
  accept(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.connectionsService.respond(user.userId, id, true);
  }

  @Patch(':id/decline')
  decline(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.connectionsService.respond(user.userId, id, false);
  }

  @Delete(':id')
  remove(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.connectionsService.remove(user.userId, id);
  }
}
