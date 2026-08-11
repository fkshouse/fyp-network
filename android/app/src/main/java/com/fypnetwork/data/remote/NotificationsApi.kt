package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.NotificationDto
import com.fypnetwork.data.remote.dto.UnreadCountDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationsApi {
    @GET("notifications")
    suspend fun list(): List<NotificationDto>

    @GET("notifications/unread-count")
    suspend fun unreadCount(): UnreadCountDto

    @PATCH("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String)

    @PATCH("notifications/read-all")
    suspend fun markAllRead()
}
