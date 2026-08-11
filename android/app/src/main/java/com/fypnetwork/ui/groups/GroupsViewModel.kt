package com.fypnetwork.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.GroupSummaryDto
import com.fypnetwork.data.repository.ConnectionsRepository
import com.fypnetwork.data.repository.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val groups: List<GroupSummaryDto> = emptyList(),
    val connections: List<ConnectionDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val connectionsRepository: ConnectionsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    // load() is called by GroupsScreen via OnResume, not from init - so
    // coming back from a group you just created/edited tasks in always
    // shows current member/task counts instead of a stale snapshot from
    // whenever the tab was first opened.
    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val groups = groupsRepository.listMine()
                _uiState.value = _uiState.value.copy(groups = groups, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load groups")
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
        // Loaded lazily since it's only needed for the "add people" step -
        // no point fetching your connections every time the tab loads.
        viewModelScope.launch {
            try {
                val connections = connectionsRepository.listAccepted()
                _uiState.value = _uiState.value.copy(connections = connections)
            } catch (e: Exception) {
                // non-critical - the create dialog just won't offer a member picker
            }
        }
    }

    fun dismissCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }

    fun createGroup(name: String, description: String, memberIds: List<String>) {
        if (name.isBlank()) return
        _uiState.value = _uiState.value.copy(isCreating = true)
        viewModelScope.launch {
            try {
                groupsRepository.create(name.trim(), description.trim().ifBlank { null }, memberIds)
                _uiState.value = _uiState.value.copy(isCreating = false, showCreateDialog = false)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, errorMessage = "Couldn't create group")
            }
        }
    }
}
