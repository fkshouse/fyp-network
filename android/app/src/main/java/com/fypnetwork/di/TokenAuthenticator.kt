package com.fypnetwork.di

import com.fypnetwork.data.local.TokenManager
import com.fypnetwork.data.remote.AuthApi
import com.fypnetwork.data.remote.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * On a 401, attempts a single silent token refresh and retries the original
 * request. Mirrors the rotate-refresh-token flow implemented server-side in
 * AuthService: the old refresh token is single-use and gets replaced.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't loop forever if the refreshed token also gets a 401.
        if (retryCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

        return runBlocking {
            try {
                val result = authApi.refresh(RefreshRequest(refreshToken))
                tokenManager.saveTokens(result.accessToken, result.refreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${result.accessToken}")
                    .build()
            } catch (e: Exception) {
                // Refresh token is invalid/expired/revoked - force a logout.
                // The UI observes TokenManager.isLoggedInFlow and will route
                // back to the login screen.
                tokenManager.clear()
                null
            }
        }
    }

    private fun retryCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
