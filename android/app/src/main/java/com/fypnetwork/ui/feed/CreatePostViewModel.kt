package com.fypnetwork.ui.feed

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.repository.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class CreatePostUiState(
    val content: String = "",
    val imageUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val postCreated: Boolean = false,
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun onContentChange(value: String) {
        _uiState.value = _uiState.value.copy(content = value, errorMessage = null)
    }

    fun onImagesPicked(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(imageUris = uris)
    }

    fun submit() {
        val state = _uiState.value
        if (state.content.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Write something before posting")
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val files = copyUrisToCache(state.imageUris)
                postsRepository.createPost(state.content.trim(), files)
                _uiState.value = _uiState.value.copy(isSubmitting = false, postCreated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Couldn't publish the post. Check your connection and try again.",
                )
            }
        }
    }

    // Retrofit's multipart body needs java.io.File-backed parts, so content:// URIs
    // from the photo picker get copied into the app's cache dir first.
    private suspend fun copyUrisToCache(uris: List<Uri>): List<File> = withContext(Dispatchers.IO) {
        uris.mapIndexed { index, uri ->
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$index.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        }
    }
}
