package com.fypnetwork.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.remote.dto.UserDto
import com.fypnetwork.data.repository.ConnectionsRepository
import com.fypnetwork.data.repository.PostsRepository
import com.fypnetwork.data.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val user: UserDto? = null,
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // NONE | PENDING_SENT | PENDING_RECEIVED | CONNECTED - queried fresh from
    // the server on every load, rather than an ephemeral "did I just tap this
    // button" flag, so it survives closing and reopening the profile.
    val connectionStatus: String = "NONE",
    val connectionId: String? = null,
    val isUpdatingConnection: Boolean = false,
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val postsRepository: PostsRepository,
    private val connectionsRepository: ConnectionsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val user = usersRepository.getUser(userId)
                val posts = postsRepository.getFeed(authorId = userId).items
                val status = connectionsRepository.getStatus(userId)
                _uiState.value = _uiState.value.copy(
                    user = user,
                    posts = posts,
                    connectionStatus = status.status,
                    connectionId = status.connectionId,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load this profile")
            }
        }
    }

    fun sendConnectionRequest() {
        _uiState.value = _uiState.value.copy(isUpdatingConnection = true)
        viewModelScope.launch {
            try {
                connectionsRepository.sendRequest(userId)
                _uiState.value = _uiState.value.copy(connectionStatus = "PENDING_SENT", isUpdatingConnection = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdatingConnection = false, errorMessage = "Couldn't send connection request")
            }
        }
    }

    fun removeConnection() {
        val connectionId = _uiState.value.connectionId ?: return
        _uiState.value = _uiState.value.copy(isUpdatingConnection = true)
        viewModelScope.launch {
            try {
                connectionsRepository.remove(connectionId)
                _uiState.value = _uiState.value.copy(
                    connectionStatus = "NONE",
                    connectionId = null,
                    isUpdatingConnection = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdatingConnection = false, errorMessage = "Couldn't remove connection")
            }
        }
    }

    fun acceptRequest() {
        val connectionId = _uiState.value.connectionId ?: return
        _uiState.value = _uiState.value.copy(isUpdatingConnection = true)
        viewModelScope.launch {
            try {
                connectionsRepository.accept(connectionId)
                _uiState.value = _uiState.value.copy(connectionStatus = "CONNECTED", isUpdatingConnection = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdatingConnection = false, errorMessage = "Couldn't accept request")
            }
        }
    }
}
