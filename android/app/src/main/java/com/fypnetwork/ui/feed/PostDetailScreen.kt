package com.fypnetwork.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fypnetwork.data.remote.dto.CommentDto
import com.fypnetwork.ui.theme.BrandError
import com.fypnetwork.util.DateTimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.commentDraft,
                    onValueChange = viewModel::onCommentDraftChange,
                    placeholder = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = viewModel::submitComment, enabled = !uiState.isSubmittingComment) {
                    Icon(Icons.Filled.Send, contentDescription = "Send comment")
                }
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.errorMessage != null && uiState.post == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "", color = BrandError)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            uiState.post?.let { post ->
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.author.profilePictureUrl != null) {
                                AsyncImage(
                                    model = post.author.profilePictureUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(44.dp).clip(CircleShape),
                                )
                            } else {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, contentDescription = null)
                                }
                            }
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(post.author.name, style = MaterialTheme.typography.titleMedium)
                                Text(DateTimeFormat.full(post.createdAt), style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Text(post.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))

                        if (post.media.isNotEmpty()) {
                            AsyncImage(
                                model = post.media.first().url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }

                        Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::toggleLike) {
                                Icon(
                                    imageVector = if (post.likedByViewer) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (post.likedByViewer) BrandError else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text("${post.likeCount} likes", style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(
                            "Comments (${uiState.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }

            items(uiState.comments, key = { it.id }) { comment ->
                CommentRow(comment)
            }
        }
    }
}

@Composable
private fun CommentRow(comment: CommentDto) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (comment.author.profilePictureUrl != null) {
            AsyncImage(
                model = comment.author.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(CircleShape),
            )
        } else {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(comment.author.name, style = MaterialTheme.typography.titleMedium)
                Text(DateTimeFormat.relative(comment.createdAt), style = MaterialTheme.typography.labelSmall)
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
