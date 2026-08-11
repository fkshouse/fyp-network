package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.NotificationsApi
import com.fypnetwork.data.remote.dto.NotificationDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val notificationsApi: NotificationsApi,
) {
    suspend fun list(): List<NotificationDto> = notificationsApi.list()

    suspend fun unreadCount(): Int = notificationsApi.unreadCount().count

    suspend fun markRead(id: String) = notificationsApi.markRead(id)

    suspend fun markAllRead() = notificationsApi.markAllRead()
}
