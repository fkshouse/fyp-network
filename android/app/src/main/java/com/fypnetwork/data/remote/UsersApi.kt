package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.ChangePasswordRequest
import com.fypnetwork.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Built on the AUTHENTICATED Retrofit client, unlike AuthApi. GET /auth/me
// and PATCH /auth/password both require a valid bearer token, so they're
// grouped here rather than on AuthApi (which intentionally has no auth
// interceptor attached - see the note on AuthApi for why).
interface UsersApi {
    @GET("auth/me")
    suspend fun me(): UserDto

    @PATCH("auth/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest)

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto

    @GET("users/search")
    suspend fun search(@Query("q") query: String): List<UserDto>

    @PATCH("users/me")
    suspend fun updateMe(@Body body: Map<String, String>): UserDto

    @Multipart
    @POST("users/me/profile-picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): UserDto
}
