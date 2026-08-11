package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.TasksApi
import com.fypnetwork.data.remote.dto.CreateTaskRequest
import com.fypnetwork.data.remote.dto.TaskDto
import com.fypnetwork.data.remote.dto.UpdateTaskRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    private val tasksApi: TasksApi,
) {
    suspend fun listForGroup(groupId: String): List<TaskDto> = tasksApi.listForGroup(groupId)

    suspend fun create(
        groupId: String,
        title: String,
        description: String?,
        assigneeId: String?,
        dueDate: String?,
    ): TaskDto = tasksApi.create(groupId, CreateTaskRequest(title, description, assigneeId, dueDate))

    suspend fun updateStatus(taskId: String, status: String): TaskDto =
        tasksApi.update(taskId, UpdateTaskRequest(status = status))

    // Full edit - title/description/dueDate/assignee/completion in one call,
    // used by the task detail screen. Fields left null are left unchanged
    // server-side (see UpdateTaskDto - it only touches what's actually sent).
    suspend fun update(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        assigneeId: String? = null,
        dueDate: String? = null,
        completionPercent: Int? = null,
    ): TaskDto = tasksApi.update(
        taskId,
        UpdateTaskRequest(
            title = title,
            description = description,
            status = status,
            assigneeId = assigneeId,
            dueDate = dueDate,
            completionPercent = completionPercent,
        ),
    )

    suspend fun delete(taskId: String) = tasksApi.delete(taskId)
}
