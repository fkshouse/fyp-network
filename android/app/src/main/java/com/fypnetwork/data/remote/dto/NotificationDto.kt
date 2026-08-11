package com.fypnetwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationActorDto(
    val id: String,
    val name: String,
    val profilePictureUrl: String? = null,
)

@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val postId: String? = null,
    val connectionId: String? = null,
    val taskId: String? = null,
    val groupId: String? = null,
    val actor: NotificationActorDto? = null,
)

@Serializable
data class UnreadCountDto(
    val count: Int,
)
