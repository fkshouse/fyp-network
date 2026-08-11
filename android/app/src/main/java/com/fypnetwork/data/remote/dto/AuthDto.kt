package com.fypnetwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

// Register intentionally does NOT return tokens - registering and logging in
// are separate actions now, so the user explicitly signs in afterward.
@Serializable
data class RegisterResponse(
    val user: UserDto,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val headline: String? = null,
    val company: String? = null,
    val bio: String? = null,
    val profilePictureUrl: String? = null,
    // Only populated by the search endpoint - NONE/PENDING_SENT/PENDING_RECEIVED/CONNECTED.
    // Null on every other endpoint that returns a UserDto (profile lookups, /auth/me, etc.)
    val connectionStatus: String? = null,
    val connectionId: String? = null,
)
