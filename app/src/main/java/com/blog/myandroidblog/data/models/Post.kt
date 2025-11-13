package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val status: String,
    val user_id: String,
    val author_name: String,
    val created_at: Long,
    val updated_at: Long,
    val attachments: List<FileAttachment>? = null
)

@Serializable
data class PostListResponse(
    val data: List<Post>,
    val pagination: Pagination
)

@Serializable
data class CreatePostRequest(
    val title: String,
    val content: String,
    val status: String = "draft"
)

@Serializable
data class UpdatePostRequest(
    val title: String? = null,
    val content: String? = null,
    val status: String? = null
)

@Serializable
data class PostResponse(
    val success: Boolean,
    val message: String,
    val post: Post? = null
)

@Serializable
data class FileAttachment(
    val id: String,
    val name: String,
    val url: String,
    val size: Long,
    val mimeType: String,
    val downloadUrl: String? = null
)