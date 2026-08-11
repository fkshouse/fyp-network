package com.fypnetwork.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.repository.AuthRepository
import com.fypnetwork.data.repository.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val posts: List<PostDto> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val nextCursor: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        // Used only to know which posts are "mine" (for showing the edit/delete
        // menu) - there is deliberately no logout call anywhere in this
        // ViewModel; that lives on the Profile screen now.
        viewModelScope.launch {
            try {
                val me = authRepository.currentUser()
                _uiState.value = _uiState.value.copy(currentUserId = me.id)
            } catch (e: Exception) {
                // non-critical - own-post actions just won't show up
            }
        }
        // loadFeed() is NOT called here - FeedScreen triggers it via OnResume,
        // so returning from CreatePostScreen (or anywhere else) always shows
        // an up-to-date feed instead of a stale list from first load.
    }

    fun loadFeed() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = postsRepository.getFeed()
                _uiState.value = _uiState.value.copy(
                    posts = response.items,
                    nextCursor = response.nextCursor,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Couldn't load the feed. Pull down to try again.",
                )
            }
        }
    }

    // Separate from loadFeed() so pull-to-refresh can show its own spinner
    // instead of replacing the whole screen with a full-page loading state.
    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = postsRepository.getFeed()
                _uiState.value = _uiState.value.copy(
                    posts = response.items,
                    nextCursor = response.nextCursor,
                    isRefreshing = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun loadMore() {
        val cursor = _uiState.value.nextCursor
        if (cursor == null || _uiState.value.isLoadingMore) return

        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val response = postsRepository.getFeed(cursor = cursor)
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts + response.items,
                    nextCursor = response.nextCursor,
                    isLoadingMore = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    fun toggleLike(postId: String) {
        // Optimistic update so the tap feels instant, reconciled with the
        // server response (or rolled back silently on failure).
        val current = _uiState.value.posts
        val updated = current.map { post ->
            if (post.id == postId) {
                post.copy(
                    likedByViewer = !post.likedByViewer,
                    likeCount = if (post.likedByViewer) post.likeCount - 1 else post.likeCount + 1,
                )
            } else post
        }
        _uiState.value = _uiState.value.copy(posts = updated)

        viewModelScope.launch {
            try {
                postsRepository.toggleLike(postId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(posts = current) // roll back
            }
        }
    }

    fun editPost(postId: String, newContent: String) {
        viewModelScope.launch {
            try {
                val updated = postsRepository.updatePost(postId, newContent)
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts.map { if (it.id == postId) updated else it },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't save changes to the post")
            }
        }
    }

    fun deletePost(postId: String) {
        val current = _uiState.value.posts
        _uiState.value = _uiState.value.copy(posts = current.filterNot { it.id == postId })
        viewModelScope.launch {
            try {
                postsRepository.deletePost(postId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(posts = current, errorMessage = "Couldn't delete the post")
            }
        }
    }
}
