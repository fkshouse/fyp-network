package com.fypnetwork.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fypnetwork.data.remote.dto.ConnectionDto
import com.fypnetwork.data.remote.dto.GroupSummaryDto
import com.fypnetwork.ui.util.OnResume
import androidx.compose.foundation.lazy.LazyRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onOpenGroup: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    OnResume { viewModel.load() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Groups") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreateDialog) {
                Icon(Icons.Filled.Add, contentDescription = "Create group")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading && uiState.groups.isNotEmpty(),
            onRefresh = viewModel::load,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                uiState.isLoading && uiState.groups.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.groups.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No groups yet - tap + to create one", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.groups, key = { it.id }) { group ->
                            GroupCard(group, onClick = { onOpenGroup(group.id) })
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateGroupDialog(
                isCreating = uiState.isCreating,
                connections = uiState.connections,
                onDismiss = viewModel::dismissCreateDialog,
                onCreate = { name, desc, memberIds -> viewModel.createGroup(name, desc, memberIds) },
            )
        }
    }
}

@Composable
private fun GroupCard(group: GroupSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(group.name, style = MaterialTheme.typography.titleMedium)
            group.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(
                "${group.memberCount} members - ${group.taskCount} tasks - ${group.role.lowercase()}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun CreateGroupDialog(
    isCreating: Boolean,
    connections: List<ConnectionDto>,
    onDismiss: () -> Unit,
    onCreate: (String, String, List<String>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMemberIds by remember { mutableStateOf(setOf<String>()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New group") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (connections.isNotEmpty()) {
                    Text(
                        "Add people from your connections:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    LazyRow(modifier = Modifier.padding(top = 4.dp)) {
                        items(connections, key = { it.connectionId }) { conn ->
                            val selected = selectedMemberIds.contains(conn.user.id)
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    )
                                    .clickable {
                                        selectedMemberIds = if (selected) {
                                            selectedMemberIds - conn.user.id
                                        } else {
                                            selectedMemberIds + conn.user.id
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(conn.user.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else {
                    Text(
                        "You don't have any connections yet to add - you can add people to this group later once you're connected.",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, description, selectedMemberIds.toList()) },
                enabled = !isCreating && name.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
