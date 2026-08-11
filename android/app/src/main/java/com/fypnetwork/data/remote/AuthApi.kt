package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.AuthResponse
import com.fypnetwork.data.remote.dto.LoginRequest
import com.fypnetwork.data.remote.dto.RefreshRequest
import com.fypnetwork.data.remote.dto.RegisterRequest
import com.fypnetwork.data.remote.dto.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Deliberately built on the UNAUTHENTICATED Retrofit client (see NetworkModule) -
// register/login/refresh all happen before we have a valid access token, and
// refresh specifically must not go through the authenticated client or it'd
// create a circular dependency (refreshing a token would itself trigger
// another refresh attempt on a 401). GET /auth/me and password changes DO
// need a bearer token, so they live on UsersApi instead, which is built on
// the authenticated client - see the note there for why this split exists.
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse
}
