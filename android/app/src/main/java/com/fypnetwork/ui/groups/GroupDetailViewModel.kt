package com.fypnetwork.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.GroupDetailDto
import com.fypnetwork.data.remote.dto.TaskDto
import com.fypnetwork.data.repository.AuthRepository
import com.fypnetwork.data.repository.ConnectionsRepository
import com.fypnetwork.data.repository.GroupsRepository
import com.fypnetwork.data.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: GroupDetailDto? = null,
    val tasks: List<TaskDto> = emptyList(),
    val myRole: String? = null, // OWNER / ADMIN / MEMBER - drives whether edit/delete controls show at all
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showCreateTaskDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val showManageMembersDialog: Boolean = false,
    // Connections not already in this group - the candidates for "add member".
    val connectionsToAdd: List<ConnectionDto> = emptyList(),
    val selectedTask: TaskDto? = null, // drives the task detail/edit sheet
    val groupDeleted: Boolean = false, // screen observes this to navigate back
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val tasksRepository: TasksRepository,
    private val authRepository: AuthRepository,
    private val connectionsRepository: ConnectionsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val group = groupsRepository.getDetail(groupId)
                val tasks = tasksRepository.listForGroup(groupId)
                val me = authRepository.currentUser()
                val myRole = group.members.find { it.userId == me.id }?.role
                _uiState.value = _uiState.value.copy(group = group, tasks = tasks, myRole = myRole, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load group")
            }
        }
    }

    fun showEditGroupDialog() { _uiState.value = _uiState.value.copy(showEditGroupDialog = true) }
    fun dismissEditGroupDialog() { _uiState.value = _uiState.value.copy(showEditGroupDialog = false) }

    fun editGroup(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val updated = groupsRepository.update(groupId, name.trim(), description.trim().ifBlank { null })
                _uiState.value = _uiState.value.copy(group = updated, showEditGroupDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't save group changes")
            }
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            try {
                groupsRepository.delete(groupId)
                _uiState.value = _uiState.value.copy(groupDeleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't delete group")
            }
        }
    }

    fun showManageMembersDialog() {
        _uiState.value = _uiState.value.copy(showManageMembersDialog = true)
        viewModelScope.launch {
            try {
                val existingMemberIds = _uiState.value.group?.members.orEmpty().map { it.userId }.toSet()
                val connections = connectionsRepository.listAccepted()
                _uiState.value = _uiState.value.copy(
                    connectionsToAdd = connections.filterNot { existingMemberIds.contains(it.user.id) },
                )
            } catch (e: Exception) {
                // non-critical - the "add member" list just won't populate this time
            }
        }
    }

    fun dismissManageMembersDialog() { _uiState.value = _uiState.value.copy(showManageMembersDialog = false) }

    fun addMember(userId: String) {
        viewModelScope.launch {
            try {
                val updatedGroup = groupsRepository.addMember(groupId, userId)
                _uiState.value = _uiState.value.copy(
                    group = updatedGroup,
                    connectionsToAdd = _uiState.value.connectionsToAdd.filterNot { it.user.id == userId },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't add member")
            }
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
            try {
                groupsRepository.removeMember(groupId, userId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't remove member")
            }
        }
    }

    fun showCreateTaskDialog() { _uiState.value = _uiState.value.copy(showCreateTaskDialog = true) }
    fun dismissCreateTaskDialog() { _uiState.value = _uiState.value.copy(showCreateTaskDialog = false) }

    fun createTask(title: String, description: String, assigneeId: String?, dueDate: String?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                tasksRepository.create(groupId, title.trim(), description.trim().ifBlank { null }, assigneeId, dueDate)
                _uiState.value = _uiState.value.copy(showCreateTaskDialog = false)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't create task")
            }
        }
    }

    fun openTask(task: TaskDto) { _uiState.value = _uiState.value.copy(selectedTask = task) }
    fun closeTaskDetail() { _uiState.value = _uiState.value.copy(selectedTask = null) }

    fun updateTaskStatus(taskId: String, status: String) {
        applyOptimisticUpdate(taskId) { it.copy(status = status) }
        viewModelScope.launch {
            try {
                val updated = tasksRepository.update(taskId, status = status)
                applyServerUpdate(updated)
            } catch (e: Exception) {
                load()
            }
        }
    }

    fun updateTaskCompletion(taskId: String, percent: Int) {
        applyOptimisticUpdate(taskId) { it.copy(completionPercent = percent) }
        viewModelScope.launch {
            try {
                val updated = tasksRepository.update(taskId, completionPercent = percent)
                applyServerUpdate(updated)
            } catch (e: Exception) {
                load()
            }
        }
    }

    fun updateTaskDetails(taskId: String, title: String, description: String, dueDate: String?, assigneeId: String?) {
        viewModelScope.launch {
            try {
                val updated = tasksRepository.update(
                    taskId,
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    dueDate = dueDate,
                    assigneeId = assigneeId,
                )
                applyServerUpdate(updated)
                _uiState.value = _uiState.value.copy(selectedTask = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't save task changes")
            }
        }
    }

    fun deleteTask(taskId: String) {
        val current = _uiState.value.tasks
        _uiState.value = _uiState.value.copy(
            tasks = current.filterNot { it.id == taskId },
            selectedTask = null,
        )
        viewModelScope.launch {
            try {
                tasksRepository.delete(taskId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(tasks = current, errorMessage = "Couldn't delete task")
            }
        }
    }

    private fun applyOptimisticUpdate(taskId: String, transform: (TaskDto) -> TaskDto) {
        val updatedTasks = _uiState.value.tasks.map { if (it.id == taskId) transform(it) else it }
        val updatedSelected = _uiState.value.selectedTask?.let { if (it.id == taskId) transform(it) else it }
        _uiState.value = _uiState.value.copy(tasks = updatedTasks, selectedTask = updatedSelected)
    }

    private fun applyServerUpdate(task: TaskDto) {
        val updatedTasks = _uiState.value.tasks.map { if (it.id == task.id) task else it }
        val updatedSelected = _uiState.value.selectedTask?.let { if (it.id == task.id) task else it }
        _uiState.value = _uiState.value.copy(tasks = updatedTasks, selectedTask = updatedSelected)
    }
}
