package com.fypnetwork.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.ui.theme.BrandError
import com.fypnetwork.ui.theme.Violet
import com.fypnetwork.ui.theme.brandGradientBackground
import com.fypnetwork.ui.theme.brandPrimaryBrush
import com.fypnetwork.ui.util.OnResume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onViewConnections: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    OnResume { if (!uiState.isEditing) viewModel.load() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.onProfilePictureSelected(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onViewConnections) {
                        Icon(Icons.Filled.People, contentDescription = "Connections")
                    }
                    if (!uiState.isEditing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log out")
                    }
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

        val user = uiState.user

        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = viewModel::load,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .brandGradientBackground(),
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-56).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val pictureUrl = user?.profilePictureUrl
                            if (pictureUrl != null) {
                                AsyncImage(
                                    model = pictureUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(brandPrimaryBrush()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Text("Change photo", color = Violet, fontWeight = FontWeight.Bold)
                        }

                        user?.let {
                            Text(
                                "${it.firstName} ${it.lastName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(it.email, style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier.padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                        ) {
                            ProfileStat(count = uiState.posts.size, label = "Posts")
                        }

                        if (uiState.isEditing) {
                            OutlinedTextField(
                                value = uiState.headline,
                                onValueChange = viewModel::onHeadlineChange,
                                label = { Text("Headline") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            )
                            OutlinedTextField(
                                value = uiState.company,
                                onValueChange = viewModel::onCompanyChange,
                                label = { Text("Company") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )
                            OutlinedTextField(
                                value = uiState.bio,
                                onValueChange = viewModel::onBioChange,
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).size(120.dp),
                            )

                            uiState.errorMessage?.let {
                                Text(it, color = BrandError, modifier = Modifier.padding(top = 8.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                OutlinedButton(onClick = viewModel::cancelEditing, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = viewModel::save,
                                    enabled = !uiState.isSaving,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("Save")
                                    }
                                }
                            }
                        } else {
                            // Read-only view - fields are not editable until "Edit profile" is tapped.
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                ProfileField("Headline", uiState.headline)
                                ProfileField("Company", uiState.company)
                                ProfileField("Bio", uiState.bio)
                            }

                            TextButton(
                                onClick = viewModel::showPasswordDialog,
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Change password", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                    HorizontalDivider()
                    Text("Your posts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }

                if (uiState.posts.isEmpty()) {
                    item {
                        Text(
                            "You haven't posted anything yet",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(uiState.posts, key = { it.id }) { post -> ProfilePostRow(post) }
                }
            }
        }
    }

    if (uiState.showPasswordDialog) {
        ChangePasswordDialog(
            isChanging = uiState.isChangingPassword,
            error = uiState.passwordChangeError,
            onDismiss = viewModel::dismissPasswordDialog,
            onSubmit = { current, new -> viewModel.changePassword(current, new) },
        )
    }
}

@Composable
private fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Violet)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ProfilePostRow(post: PostDto) {
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

@Composable
private fun ChangePasswordDialog(
    isChanging: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("New password (min. 8 characters)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
                error?.let { Text(it, color = BrandError, modifier = Modifier.padding(top = 8.dp)) }
                Text(
                    "You'll be logged out on this and any other device after changing your password.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(current, new) },
                enabled = !isChanging && current.isNotBlank() && new.isNotBlank(),
            ) {
                Text("Change password")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
