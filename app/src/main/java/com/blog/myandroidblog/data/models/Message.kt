package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val user_id: String,
    val image_path: String? = null,
    val created_at: Long,
    val updated_at: Long,
    val author_name: String
)

@Serializable
data class CreateMessageRequest(
    val content: String
)

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String,
    val data: Message? = null
)