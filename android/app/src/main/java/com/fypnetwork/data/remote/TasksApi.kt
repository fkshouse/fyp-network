package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.CreateTaskRequest
import com.fypnetwork.data.remote.dto.TaskDto
import com.fypnetwork.data.remote.dto.UpdateTaskRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TasksApi {
    @GET("groups/{groupId}/tasks")
    suspend fun listForGroup(@Path("groupId") groupId: String): List<TaskDto>

    @POST("groups/{groupId}/tasks")
    suspend fun create(@Path("groupId") groupId: String, @Body body: CreateTaskRequest): TaskDto

    @PATCH("tasks/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateTaskRequest): TaskDto

    @DELETE("tasks/{id}")
    suspend fun delete(@Path("id") id: String)
}
