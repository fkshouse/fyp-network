package com.fypnetwork.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        refresh()
    }

    // Called whenever the user switches tabs (see FypNavGraph) - not perfectly
    // real-time, but correct within a tab switch, which covers the case that
    // actually matters: opening Alerts and coming back should clear the badge.
    fun refresh() {
        viewModelScope.launch {
            try {
                _unreadCount.value = notificationsRepository.unreadCount()
            } catch (e: Exception) {
                // non-critical - badge just won't update this time
            }
        }
    }
}
