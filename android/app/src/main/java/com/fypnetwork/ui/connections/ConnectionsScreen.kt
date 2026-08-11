package com.fypnetwork.ui.connections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fypnetwork.data.remote.dto.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search people by name or email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.searchResults.isNotEmpty()) {
                    item { SectionHeader("Search results") }
                    items(uiState.searchResults, key = { "search_${it.id}" }) { user ->
                        SearchResultRow(
                            user,
                            onClick = { onOpenProfile(user.id) },
                            onConnect = { viewModel.sendRequest(user.id) },
                            onAccept = { user.connectionId?.let { viewModel.accept(it) } },
                            onRemove = { user.connectionId?.let { viewModel.removeConnection(user.id, it) } },
                        )
                    }
                }

                if (uiState.pending.isNotEmpty()) {
                    item { SectionHeader("Pending requests") }
                    items(uiState.pending, key = { "pending_${it.connectionId}" }) { conn ->
                        PendingRequestRow(
                            conn = conn,
                            onAccept = { viewModel.accept(conn.connectionId) },
                            onDecline = { viewModel.decline(conn.connectionId) },
                        )
                    }
                }

                item { SectionHeader("Your connections (${uiState.accepted.size})") }
                items(uiState.accepted, key = { "accepted_${it.connectionId}" }) { conn ->
                    ConnectionRow(conn, onClick = { onOpenProfile(conn.user.id) })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
}

@Composable
private fun Avatar(url: String?) {
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).clip(CircleShape),
        )
    } else {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null)
        }
    }
}

@Composable
private fun SearchResultRow(
    user: UserDto,
    onClick: () -> Unit,
    onConnect: () -> Unit,
    onAccept: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(user.profilePictureUrl)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
            user.headline?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
        when (user.connectionStatus) {
            "CONNECTED" -> OutlinedButton(onClick = onRemove) { Text("Remove") }
            "PENDING_SENT" -> Button(onClick = {}, enabled = false) { Text("Request sent") }
            "PENDING_RECEIVED" -> Button(onClick = onAccept) { Text("Accept") }
            else -> Button(onClick = onConnect) { Text("Connect") }
        }
    }
}

@Composable
private fun PendingRequestRow(conn: com.fypnetwork.data.remote.dto.ConnectionDto, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(conn.user.profilePictureUrl)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(conn.user.name, style = MaterialTheme.typography.titleMedium)
            conn.user.headline?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDecline) { Text("Decline") }
            Button(onClick = onAccept) { Text("Accept") }
        }
    }
}

@Composable
private fun ConnectionRow(conn: com.fypnetwork.data.remote.dto.ConnectionDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(conn.user.profilePictureUrl)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(conn.user.name, style = MaterialTheme.typography.titleMedium)
            conn.user.headline?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
