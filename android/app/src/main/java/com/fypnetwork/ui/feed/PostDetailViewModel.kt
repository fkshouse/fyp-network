package com.fypnetwork.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.CommentDto
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.repository.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: PostDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val commentDraft: String = "",
    val isLoading: Boolean = true,
    val isSubmittingComment: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val post = postsRepository.getPost(postId)
                val comments = postsRepository.getComments(postId)
                _uiState.value = _uiState.value.copy(post = post, comments = comments, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load this post")
            }
        }
    }

    fun onCommentDraftChange(value: String) {
        _uiState.value = _uiState.value.copy(commentDraft = value)
    }

    fun submitComment() {
        val draft = _uiState.value.commentDraft.trim()
        if (draft.isBlank()) return

        _uiState.value = _uiState.value.copy(isSubmittingComment = true)
        viewModelScope.launch {
            try {
                val comment = postsRepository.addComment(postId, draft)
                val post = _uiState.value.post
                _uiState.value = _uiState.value.copy(
                    comments = _uiState.value.comments + comment,
                    commentDraft = "",
                    isSubmittingComment = false,
                    post = post?.copy(commentCount = post.commentCount + 1),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmittingComment = false, errorMessage = "Couldn't post your comment")
            }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post ?: return
        val updated = post.copy(
            likedByViewer = !post.likedByViewer,
            likeCount = if (post.likedByViewer) post.likeCount - 1 else post.likeCount + 1,
        )
        _uiState.value = _uiState.value.copy(post = updated)

        viewModelScope.launch {
            try {
                postsRepository.toggleLike(postId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(post = post) // roll back
            }
        }
    }
}
