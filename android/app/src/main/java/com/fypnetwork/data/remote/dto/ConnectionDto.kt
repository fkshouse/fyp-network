package com.fypnetwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionUserDto(
    val id: String,
    val name: String,
    val headline: String? = null,
    val profilePictureUrl: String? = null,
)

@Serializable
data class ConnectionDto(
    val connectionId: String,
    val user: ConnectionUserDto,
    val createdAt: String? = null,
)

@Serializable
data class SendConnectionRequestBody(
    val addresseeId: String,
)

@Serializable
data class ConnectionStatusDto(
    val status: String, // SELF | NONE | PENDING_SENT | PENDING_RECEIVED | CONNECTED
    val connectionId: String? = null,
)
