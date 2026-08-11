package com.fypnetwork.data.repository

import com.fypnetwork.data.local.TokenManager
import com.fypnetwork.data.remote.AuthApi
import com.fypnetwork.data.remote.UsersApi
import com.fypnetwork.data.remote.dto.ChangePasswordRequest
import com.fypnetwork.data.remote.dto.LoginRequest
import com.fypnetwork.data.remote.dto.RegisterRequest
import com.fypnetwork.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val tokenManager: TokenManager,
) {
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedInFlow

    // Register does NOT sign the user in - it only creates the account. The
    // caller (RegisterViewModel) sends them to the login screen afterward,
    // rather than us silently storing tokens here.
    suspend fun register(email: String, password: String, firstName: String, lastName: String): UserDto {
        val response = authApi.register(RegisterRequest(email, password, firstName, lastName))
        return response.user
    }

    suspend fun login(email: String, password: String): UserDto {
        val response = authApi.login(LoginRequest(email, password))
        tokenManager.saveTokens(response.accessToken, response.refreshToken)
        return response.user
    }

    // Goes through UsersApi (authenticated client), not AuthApi - see the
    // note on AuthApi for why GET /auth/me can't live there.
    suspend fun currentUser(): UserDto = usersApi.me()

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        usersApi.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        // Server-side, changing the password revokes all existing refresh
        // tokens (including this device's) - so log out locally too rather
        // than leaving the app holding a token that's about to stop working.
        tokenManager.clear()
    }

    suspend fun logout() {
        tokenManager.clear()
    }
}
