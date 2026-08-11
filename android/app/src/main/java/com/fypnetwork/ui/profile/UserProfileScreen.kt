package com.fypnetwork.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.fypnetwork.data.remote.dto.PostDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.user?.let { "${it.firstName} ${it.lastName}" } ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val user = uiState.user ?: return@Scaffold

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (user.profilePictureUrl != null) {
                        AsyncImage(
                            model = user.profilePictureUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(88.dp).clip(CircleShape),
                        )
                    } else {
                        Box(modifier = Modifier.size(88.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(56.dp))
                        }
                    }
                    Text(
                        "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    user.headline?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    user.company?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    user.bio?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    }

                    ConnectionActionButton(
                        status = uiState.connectionStatus,
                        isUpdating = uiState.isUpdatingConnection,
                        onConnect = viewModel::sendConnectionRequest,
                        onRemove = viewModel::removeConnection,
                        onAccept = viewModel::acceptRequest,
                    )
                }
                HorizontalDivider()
                Text(
                    "Posts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (uiState.posts.isEmpty()) {
                item {
                    Text(
                        "No posts yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(uiState.posts, key = { it.id }) { post ->
                    UserPostRow(post)
                }
            }
        }
    }
}

@Composable
private fun ConnectionActionButton(
    status: String,
    isUpdating: Boolean,
    onConnect: () -> Unit,
    onRemove: () -> Unit,
    onAccept: () -> Unit,
) {
    when (status) {
        "CONNECTED" -> {
            OutlinedButton(onClick = onRemove, enabled = !isUpdating, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Filled.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Remove connection", modifier = Modifier.padding(start = 6.dp))
            }
        }
        "PENDING_SENT" -> {
            Button(onClick = {}, enabled = false, modifier = Modifier.padding(top = 12.dp)) {
                Text("Request sent")
            }
        }
        "PENDING_RECEIVED" -> {
            Button(onClick = onAccept, enabled = !isUpdating, modifier = Modifier.padding(top = 12.dp)) {
                Text("Accept request")
            }
        }
        "SELF" -> {
            // Viewing your own profile through this screen shouldn't normally
            // happen (Profile tab is used for that instead), but handle it
            // gracefully rather than showing a nonsensical "Connect" button.
        }
        else -> {
            Button(onClick = onConnect, enabled = !isUpdating, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Connect", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun UserPostRow(post: PostDto) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(post.content, style = MaterialTheme.typography.bodyLarge)
        if (post.media.isNotEmpty()) {
            AsyncImage(
                model = post.media.first().url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
        Text(
            "${post.likeCount} likes - ${post.commentCount} comments",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    HorizontalDivider()
}
