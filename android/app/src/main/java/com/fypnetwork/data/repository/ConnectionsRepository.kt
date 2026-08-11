package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.ConnectionsApi
import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.ConnectionStatusDto
import com.fypnetwork.data.remote.dto.SendConnectionRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionsRepository @Inject constructor(
    private val connectionsApi: ConnectionsApi,
) {
    suspend fun sendRequest(addresseeId: String) =
        connectionsApi.sendRequest(SendConnectionRequestBody(addresseeId))

    suspend fun listAccepted(): List<ConnectionDto> = connectionsApi.listAccepted()

    suspend fun listPending(): List<ConnectionDto> = connectionsApi.listPending()

    suspend fun getStatus(userId: String): ConnectionStatusDto = connectionsApi.getStatus(userId)

    suspend fun accept(connectionId: String) = connectionsApi.accept(connectionId)

    suspend fun decline(connectionId: String) = connectionsApi.decline(connectionId)

    suspend fun remove(connectionId: String) = connectionsApi.remove(connectionId)
}
