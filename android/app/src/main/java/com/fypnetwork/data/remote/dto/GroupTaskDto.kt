package com.fypnetwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupSummaryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val role: String,
    val memberCount: Int,
    val taskCount: Int,
)

@Serializable
data class GroupMemberDto(
    val userId: String,
    val name: String,
    val role: String,
    val profilePictureUrl: String? = null,
)

@Serializable
data class GroupDetailDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val members: List<GroupMemberDto>,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val memberIds: List<String>? = null,
)

@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
)

@Serializable
data class AddMemberRequest(
    val userId: String,
)

@Serializable
data class TaskAssigneeDto(
    val id: String,
    val name: String,
    val profilePictureUrl: String? = null,
)

@Serializable
data class TaskDto(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String? = null,
    val status: String, // TODO | IN_PROGRESS | DONE
    val completionPercent: Int = 0,
    val dueDate: String? = null,
    val createdAt: String,
    val createdBy: TaskAssigneeDto,
    val assignee: TaskAssigneeDto? = null,
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val assigneeId: String? = null,
    val dueDate: String? = null,
)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val assigneeId: String? = null,
    val dueDate: String? = null,
    val completionPercent: Int? = null,
)
