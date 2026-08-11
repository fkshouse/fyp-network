package com.fypnetwork.ui.feed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.ui.theme.BrandError
import com.fypnetwork.ui.theme.HotPink
import com.fypnetwork.ui.theme.LikeRed
import com.fypnetwork.ui.theme.Violet
import com.fypnetwork.ui.theme.brandPrimaryBrush
import com.fypnetwork.ui.util.OnResume
import com.fypnetwork.util.DateTimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onCreatePost: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingPost by remember { mutableStateOf<PostDto?>(null) }
    var confirmDeletePost by remember { mutableStateOf<PostDto?>(null) }

    OnResume { viewModel.loadFeed() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FYP Network",
                        style = MaterialTheme.typography.titleLarge,
                        color = Violet,
                        fontWeight = FontWeight.Black,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(brandPrimaryBrush())
                    .clickable(onClick = onCreatePost),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create post", tint = Color.White)
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                uiState.isLoading && uiState.posts.isEmpty() -> {
                    CircularProgressIndicator(color = Violet, modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null && uiState.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(uiState.errorMessage ?: "", color = BrandError)
                    }
                }
                uiState.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("No posts yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Be the first to share something",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                isOwnPost = post.author.id == uiState.currentUserId,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onCommentClick = { onOpenPost(post.id) },
                                onAuthorClick = { onOpenProfile(post.author.id) },
                                onEditClick = { editingPost = post },
                                onDeleteClick = { confirmDeletePost = post },
                            )
                        }
                        item {
                            if (uiState.nextCursor != null) {
                                LaunchedEffect(Unit) { viewModel.loadMore() }
                            }
                            if (uiState.isLoadingMore) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Violet)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingPost?.let { post ->
        EditPostDialog(
            initialContent = post.content,
            onDismiss = { editingPost = null },
            onSave = { newContent ->
                viewModel.editPost(post.id, newContent)
                editingPost = null
            },
        )
    }

    confirmDeletePost?.let { post ->
        AlertDialog(
            onDismissRequest = { confirmDeletePost = null },
            title = { Text("Delete post?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deletePost(post.id)
                    confirmDeletePost = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDeletePost = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EditPostDialog(initialContent: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var content by remember { mutableStateOf(initialContent) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit post") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onSave(content) }, enabled = content.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PostCard(
    post: PostDto,
    isOwnPost: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f).clickable(onClick = onAuthorClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(brandPrimaryBrush())
                            .padding(2.dp),
                    ) {
                        if (post.author.profilePictureUrl != null) {
                            AsyncImage(
                                model = post.author.profilePictureUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White),
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Violet)
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(post.author.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(DateTimeFormat.relative(post.createdAt), style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (isOwnPost) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Post options")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEditClick() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; onDeleteClick() })
                        }
                    }
                }
            }

            Text(
                post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )

            if (post.media.isNotEmpty()) {
                AsyncImage(
                    model = post.media.first().url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .padding(top = 12.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedLikeButton(liked = post.likedByViewer, onClick = onLikeClick)
                Text("${post.likeCount}", style = MaterialTheme.typography.labelLarge)

                IconButton(onClick = onCommentClick, modifier = Modifier.padding(start = 16.dp)) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Comments", tint = HotPink)
                }
                Text("${post.commentCount}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AnimatedLikeButton(liked: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (liked) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale",
    )
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Like",
            tint = if (liked) LikeRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
        )
    }
}
