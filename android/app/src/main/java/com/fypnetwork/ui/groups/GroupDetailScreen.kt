package com.fypnetwork.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fypnetwork.data.remote.dto.GroupMemberDto
import com.fypnetwork.data.remote.dto.TaskDto
import com.fypnetwork.ui.theme.BrandError
import com.fypnetwork.ui.theme.BrandPrimary
import com.fypnetwork.ui.theme.BrandSecondary
import com.fypnetwork.ui.util.OnResume
import com.fypnetwork.util.DateTimeFormat
import java.time.Instant
import java.time.ZoneOffset

private val STATUS_OPTIONS = listOf("TODO" to "To do", "IN_PROGRESS" to "In progress", "DONE" to "Done")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    OnResume { viewModel.load() }

    LaunchedEffect(uiState.groupDeleted) {
        if (uiState.groupDeleted) onBack()
    }

    val canEdit = uiState.myRole == "OWNER" || uiState.myRole == "ADMIN"
    val canDelete = uiState.myRole == "OWNER"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (canEdit) {
                        IconButton(onClick = viewModel::showEditGroupDialog) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit group")
                        }
                    }
                    if (canEdit) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Group options")
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Manage members") },
                                    onClick = { menuExpanded = false; viewModel.showManageMembersDialog() },
                                )
                                if (canDelete) {
                                    DropdownMenuItem(
                                        text = { Text("Delete group") },
                                        onClick = { menuExpanded = false; showDeleteConfirm = true },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreateTaskDialog) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            uiState.group?.let { group ->
                Text(
                    "${group.members.size} members - tap a task for details",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(group.members, key = { it.userId }) { member ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text("${member.name} - ${member.role.lowercase()}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            TaskBoard(tasks = uiState.tasks, onTaskClick = viewModel::openTask)
        }

        if (uiState.showCreateTaskDialog) {
            CreateTaskDialog(
                assigneeOptions = uiState.group?.members.orEmpty(),
                onDismiss = viewModel::dismissCreateTaskDialog,
                onCreate = { title, desc, assigneeId, dueDate -> viewModel.createTask(title, desc, assigneeId, dueDate) },
            )
        }

        uiState.selectedTask?.let { task ->
            TaskDetailDialog(
                task = task,
                assigneeOptions = uiState.group?.members.orEmpty(),
                onDismiss = viewModel::closeTaskDetail,
                onStatusChange = { status -> viewModel.updateTaskStatus(task.id, status) },
                onCompletionChange = { percent -> viewModel.updateTaskCompletion(task.id, percent) },
                onSaveDetails = { title, desc, dueDate, assigneeId ->
                    viewModel.updateTaskDetails(task.id, title, desc, dueDate, assigneeId)
                },
                onDelete = { viewModel.deleteTask(task.id) },
            )
        }

        if (uiState.showEditGroupDialog) {
            EditGroupDialog(
                initialName = uiState.group?.name.orEmpty(),
                initialDescription = uiState.group?.description.orEmpty(),
                onDismiss = viewModel::dismissEditGroupDialog,
                onSave = { name, desc -> viewModel.editGroup(name, desc) },
            )
        }

        if (uiState.showManageMembersDialog) {
            ManageMembersDialog(
                members = uiState.group?.members.orEmpty(),
                connectionsToAdd = uiState.connectionsToAdd,
                onDismiss = viewModel::dismissManageMembersDialog,
                onAdd = viewModel::addMember,
                onRemove = viewModel::removeMember,
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete this group?") },
                text = { Text("This deletes the group, its tasks, and membership for everyone. This can't be undone.") },
                confirmButton = {
                    Button(onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteGroup()
                    }) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun EditGroupDialog(
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit group") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, description) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ManageMembersDialog(
    members: List<GroupMemberDto>,
    connectionsToAdd: List<com.fypnetwork.data.remote.dto.ConnectionDto>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage members") },
        text = {
            Column {
                Text("Current members", style = MaterialTheme.typography.titleMedium)
                members.forEach { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyLarge)
                            Text(member.role.lowercase(), style = MaterialTheme.typography.labelSmall)
                        }
                        // Owners can't be removed - matches the backend rule,
                        // which would reject this anyway, but hiding the
                        // button here avoids a confusing failed request.
                        if (member.role != "OWNER") {
                            IconButton(onClick = { onRemove(member.userId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove ${member.name}", tint = BrandError)
                            }
                        }
                    }
                }

                Text("Add people", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                if (connectionsToAdd.isEmpty()) {
                    Text(
                        "No connections available to add - everyone you're connected with is already in this group.",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    connectionsToAdd.forEach { conn ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(conn.user.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onAdd(conn.user.id) }) { Text("Add") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
    )
}

@Composable
private fun TaskBoard(tasks: List<TaskDto>, onTaskClick: (TaskDto) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        STATUS_OPTIONS.forEach { (statusKey, label) ->
            val columnTasks = tasks.filter { it.status == statusKey }
            item {
                Text(
                    "$label (${columnTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(columnTasks, key = { it.id }) { task ->
                TaskCard(task, onClick = { onTaskClick(task) })
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    task.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
                task.assignee?.let {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandSecondary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(it.name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (task.completionPercent > 0) {
                LinearProgressIndicator(
                    progress = { task.completionPercent / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Text("${task.completionPercent}% complete", style = MaterialTheme.typography.labelSmall)
            }

            task.dueDate?.let { dueDate ->
                Text(
                    DateTimeFormat.countdown(dueDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (DateTimeFormat.isOverdue(dueDate) && task.status != "DONE") BrandError else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MemberPicker(
    options: List<GroupMemberDto>,
    selectedUserId: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(modifier = Modifier.padding(top = 4.dp)) {
        items(options, key = { it.userId }) { member ->
            val selected = selectedUserId == member.userId
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .background(
                        if (selected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onSelect(if (selected) null else member.userId) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(member.name, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateField(selectedIso: String?, onChange: (String?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        OutlinedButton(onClick = { showPicker = true }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text(selectedIso?.let { DateTimeFormat.dateOnly(it) } ?: "Set due date")
        }
        if (selectedIso != null) {
            TextButton(onClick = { onChange(null) }) { Text("Clear") }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val iso = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toInstant().toString()
                        onChange(iso)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun CreateTaskDialog(
    assigneeOptions: List<GroupMemberDto>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String?, String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assigneeId by remember { mutableStateOf<String?>(null) }
    var dueDate by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text("Assign to:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                MemberPicker(assigneeOptions, assigneeId) { assigneeId = it }
                DueDateField(dueDate) { dueDate = it }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(title, description, assigneeId, dueDate) }, enabled = title.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TaskDetailDialog(
    task: TaskDto,
    assigneeOptions: List<GroupMemberDto>,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onCompletionChange: (Int) -> Unit,
    onSaveDetails: (String, String, String?, String?) -> Unit,
    onDelete: () -> Unit,
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description.orEmpty()) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate) }
    var assigneeId by remember(task.id) { mutableStateOf(task.assignee?.id) }
    var sliderValue by remember(task.id) { mutableStateOf(task.completionPercent.toFloat()) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Task details") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text("Status:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                Row {
                    STATUS_OPTIONS.forEach { (key, label) ->
                        val selected = task.status == key
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp, top = 4.dp)
                                .background(
                                    if (selected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { onStatusChange(key) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text(
                    "Completion: ${sliderValue.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onCompletionChange(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    steps = 9,
                )

                Text("Assignee:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                MemberPicker(assigneeOptions, assigneeId) { assigneeId = it }

                DueDateField(dueDate) { dueDate = it }

                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = BrandError, modifier = Modifier.padding(end = 4.dp))
                    Text("Delete task", color = BrandError)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSaveDetails(title, description, dueDate, assigneeId) }, enabled = title.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this task?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                Button(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}
