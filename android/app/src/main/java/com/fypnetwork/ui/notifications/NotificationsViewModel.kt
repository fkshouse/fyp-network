package com.fypnetwork.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.NotificationDto
import com.fypnetwork.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val notifications = notificationsRepository.list()
                _uiState.value = _uiState.value.copy(notifications = notifications, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load notifications")
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                notificationsRepository.markAllRead()
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map { it.copy(isRead = true) },
                )
            } catch (e: Exception) {
                // non-critical, ignore
            }
        }
    }

    fun markRead(id: String) {
        val updated = _uiState.value.notifications.map { if (it.id == id) it.copy(isRead = true) else it }
        _uiState.value = _uiState.value.copy(notifications = updated)
        viewModelScope.launch {
            try {
                notificationsRepository.markRead(id)
            } catch (e: Exception) {
                // non-critical, ignore
            }
        }
    }
}
