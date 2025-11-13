package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val post_id: String,
    val user_id: String,
    val content: String,
    val created_at: Long,
    val updated_at: Long,
    val author_name: String
)

@Serializable
data class CreateCommentRequest(
    val content: String
)

@Serializable
data class CommentResponse(
    val success: Boolean,
    val message: String,
    val commentId: String? = null
)