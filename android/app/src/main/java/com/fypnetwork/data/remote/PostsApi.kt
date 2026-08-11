package com.fypnetwork.data.remote

import com.fypnetwork.data.remote.dto.CommentDto
import com.fypnetwork.data.remote.dto.CreateCommentRequest
import com.fypnetwork.data.remote.dto.FeedResponse
import com.fypnetwork.data.remote.dto.LikeResponse
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.remote.dto.UpdatePostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostsApi {
    @GET("posts")
    suspend fun getFeed(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("authorId") authorId: String? = null,
    ): FeedResponse

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: String): PostDto

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part("content") content: RequestBody,
        @Part media: List<MultipartBody.Part>,
    ): PostDto

    @PATCH("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body body: UpdatePostRequest): PostDto

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String)

    @GET("posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: String): List<CommentDto>

    @POST("posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Body body: CreateCommentRequest,
    ): CommentDto

    @POST("posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: String): LikeResponse
}
