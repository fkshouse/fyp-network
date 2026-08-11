package com.fypnetwork.di

import com.fypnetwork.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp interceptors are synchronous, so a short runBlocking here is
        // the standard way to read from a suspend-based token store. This is
        // cheap: DataStore keeps the value cached in memory after first read.
        val token = runBlocking { tokenManager.getAccessToken() }

        val request = chain.request().let { original ->
            if (token != null) {
                original.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                original
            }
        }
        return chain.proceed(request)
    }
}
