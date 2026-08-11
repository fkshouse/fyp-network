package com.fypnetwork.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.UserDto
import com.fypnetwork.data.repository.ConnectionsRepository
import com.fypnetwork.data.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionsUiState(
    val pending: List<ConnectionDto> = emptyList(),
    val accepted: List<ConnectionDto> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<UserDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionsUiState())
    val uiState: StateFlow<ConnectionsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val pending = connectionsRepository.listPending()
                val accepted = connectionsRepository.listAccepted()
                _uiState.value = _uiState.value.copy(pending = pending, accepted = accepted, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load connections")
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
        if (value.trim().length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            try {
                val results = usersRepository.search(value.trim())
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                // silently ignore search failures - not critical path
            }
        }
    }

    fun sendRequest(userId: String) {
        viewModelScope.launch {
            try {
                connectionsRepository.sendRequest(userId)
                _uiState.value = _uiState.value.copy(
                    searchResults = _uiState.value.searchResults.map {
                        if (it.id == userId) it.copy(connectionStatus = "PENDING_SENT") else it
                    },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't send request")
            }
        }
    }

    fun removeConnection(userId: String, connectionId: String) {
        viewModelScope.launch {
            try {
                connectionsRepository.remove(connectionId)
                _uiState.value = _uiState.value.copy(
                    searchResults = _uiState.value.searchResults.map {
                        if (it.id == userId) it.copy(connectionStatus = "NONE", connectionId = null) else it
                    },
                )
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't remove connection")
            }
        }
    }

    fun accept(connectionId: String) {
        viewModelScope.launch {
            try {
                connectionsRepository.accept(connectionId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't accept request")
            }
        }
    }

    fun decline(connectionId: String) {
        viewModelScope.launch {
            try {
                connectionsRepository.decline(connectionId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't decline request")
            }
        }
    }
}
