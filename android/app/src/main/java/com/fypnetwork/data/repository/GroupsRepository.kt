package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.GroupsApi
import com.fypnetwork.data.remote.dto.AddMemberRequest
import com.fypnetwork.data.remote.dto.CreateGroupRequest
import com.fypnetwork.data.remote.dto.GroupDetailDto
import com.fypnetwork.data.remote.dto.GroupSummaryDto
import com.fypnetwork.data.remote.dto.UpdateGroupRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupsRepository @Inject constructor(
    private val groupsApi: GroupsApi,
) {
    suspend fun create(name: String, description: String?, memberIds: List<String> = emptyList()): GroupDetailDto =
        groupsApi.create(CreateGroupRequest(name, description, memberIds.ifEmpty { null }))

    suspend fun listMine(): List<GroupSummaryDto> = groupsApi.listMine()

    suspend fun getDetail(id: String): GroupDetailDto = groupsApi.getDetail(id)

    suspend fun update(groupId: String, name: String, description: String?): GroupDetailDto =
        groupsApi.update(groupId, UpdateGroupRequest(name, description))

    suspend fun delete(groupId: String) = groupsApi.delete(groupId)

    suspend fun addMember(groupId: String, userId: String): GroupDetailDto =
        groupsApi.addMember(groupId, AddMemberRequest(userId))

    suspend fun removeMember(groupId: String, userId: String) = groupsApi.removeMember(groupId, userId)
}
