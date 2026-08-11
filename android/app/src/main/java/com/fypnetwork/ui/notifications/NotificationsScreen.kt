package com.fypnetwork.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.fypnetwork.ui.theme.BrandPrimaryContainer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fypnetwork.data.remote.dto.NotificationDto
import com.fypnetwork.util.DateTimeFormat

// Where tapping a notification should take you, based on its type. Falls
// back to null (no navigation, just marks it read) if the data needed isn't
// present - e.g. an old notification created before groupId existed.
private fun notificationTarget(n: NotificationDto): NotificationTarget? = when (n.type) {
    "CONNECTION_REQUEST", "CONNECTION_ACCEPTED" -> NotificationTarget.Connections
    "POST_LIKE", "POST_COMMENT" -> n.postId?.let { NotificationTarget.Post(it) }
    "TASK_ASSIGNED" -> n.groupId?.let { NotificationTarget.Group(it) }
    "GROUP_ADDED" -> n.groupId?.let { NotificationTarget.Group(it) }
    else -> null
}

sealed class NotificationTarget {
    data object Connections : NotificationTarget()
    data class Post(val postId: String) : NotificationTarget()
    data class Group(val groupId: String) : NotificationTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigate: (NotificationTarget) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    IconButton(onClick = viewModel::markAllRead) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::load,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            if (uiState.notifications.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications yet", style = MaterialTheme.typography.bodyMedium)
                }
                return@PullToRefreshBox
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.notifications, key = { it.id }) { notification ->
                    NotificationRow(
                        notification,
                        onClick = {
                            viewModel.markRead(notification.id)
                            notificationTarget(notification)?.let(onNavigate)
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.Transparent else BrandPrimaryContainer.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(notification.message, style = MaterialTheme.typography.bodyLarge)
            Text(DateTimeFormat.relative(notification.createdAt), style = MaterialTheme.typography.labelSmall)
        }
    }
}
