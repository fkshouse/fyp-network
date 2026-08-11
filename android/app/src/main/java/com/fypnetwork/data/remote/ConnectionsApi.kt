package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.ConnectionStatusDto
import com.fypnetwork.data.remote.dto.SendConnectionRequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConnectionsApi {
    @POST("connections")
    suspend fun sendRequest(@Body body: SendConnectionRequestBody)

    @GET("connections")
    suspend fun listAccepted(@Query("status") status: String = "accepted"): List<ConnectionDto>

    @GET("connections")
    suspend fun listPending(@Query("status") status: String = "pending"): List<ConnectionDto>

    @GET("connections/status/{userId}")
    suspend fun getStatus(@Path("userId") userId: String): ConnectionStatusDto

    @PATCH("connections/{id}/accept")
    suspend fun accept(@Path("id") id: String)

    @PATCH("connections/{id}/decline")
    suspend fun decline(@Path("id") id: String)

    @DELETE("connections/{id}")
    suspend fun remove(@Path("id") id: String)
}
