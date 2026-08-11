package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.PostsApi
import com.fypnetwork.data.remote.dto.CommentDto
import com.fypnetwork.data.remote.dto.CreateCommentRequest
import com.fypnetwork.data.remote.dto.FeedResponse
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.remote.dto.UpdatePostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsRepository @Inject constructor(
    private val postsApi: PostsApi,
) {
    suspend fun getFeed(cursor: String? = null, limit: Int = 20, authorId: String? = null): FeedResponse =
        postsApi.getFeed(cursor, limit, authorId)

    suspend fun getPost(id: String): PostDto = postsApi.getPost(id)

    suspend fun createPost(content: String, mediaFiles: List<File>): PostDto =
        withContext(Dispatchers.IO) {
            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val mediaParts = mediaFiles.map { file ->
                // image/* is a reasonable default for now - see the video-support
                // note in the README for why this isn't sniffing real mime types yet.
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("media", file.name, requestBody)
            }
            postsApi.createPost(contentBody, mediaParts)
        }

    suspend fun updatePost(id: String, content: String): PostDto =
        postsApi.updatePost(id, UpdatePostRequest(content))

    suspend fun deletePost(id: String) = postsApi.deletePost(id)

    suspend fun getComments(postId: String): List<CommentDto> = postsApi.getComments(postId)

    suspend fun addComment(postId: String, content: String): CommentDto =
        postsApi.addComment(postId, CreateCommentRequest(content))

    suspend fun toggleLike(postId: String): Boolean = postsApi.toggleLike(postId).liked
}
