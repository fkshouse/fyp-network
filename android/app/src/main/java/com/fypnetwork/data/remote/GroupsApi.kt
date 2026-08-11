package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.AddMemberRequest
import com.fypnetwork.data.remote.dto.CreateGroupRequest
import com.fypnetwork.data.remote.dto.GroupDetailDto
import com.fypnetwork.data.remote.dto.GroupSummaryDto
import com.fypnetwork.data.remote.dto.UpdateGroupRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupsApi {
    @POST("groups")
    suspend fun create(@Body body: CreateGroupRequest): GroupDetailDto

    @GET("groups")
    suspend fun listMine(): List<GroupSummaryDto>

    @GET("groups/{id}")
    suspend fun getDetail(@Path("id") id: String): GroupDetailDto

    @PATCH("groups/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateGroupRequest): GroupDetailDto

    @DELETE("groups/{id}")
    suspend fun delete(@Path("id") id: String)

    @POST("groups/{id}/members")
    suspend fun addMember(@Path("id") id: String, @Body body: AddMemberRequest): GroupDetailDto

    @DELETE("groups/{id}/members/{userId}")
    suspend fun removeMember(@Path("id") id: String, @Path("userId") userId: String)
}
