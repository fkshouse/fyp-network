package com.fypnetwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostAuthorDto(
    val id: String,
    val name: String,
    val profilePictureUrl: String? = null,
)

@Serializable
data class MediaDto(
    val url: String,
    val mimeType: String,
)

@Serializable
data class PostDto(
    val id: String,
    val content: String,
    val createdAt: String,
    val author: PostAuthorDto,
    val media: List<MediaDto> = emptyList(),
    val commentCount: Int,
    val likeCount: Int,
    val likedByViewer: Boolean,
)

@Serializable
data class FeedResponse(
    val items: List<PostDto>,
    val nextCursor: String? = null,
)

@Serializable
data class CommentDto(
    val id: String,
    val content: String,
    val createdAt: String,
    val author: PostAuthorDto,
)

@Serializable
data class CreateCommentRequest(
    val content: String,
)

@Serializable
data class UpdatePostRequest(
    val content: String,
)

@Serializable
data class LikeResponse(
    val liked: Boolean,
)
