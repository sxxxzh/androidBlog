package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TodoComment(
    val id: Int,
    val todo_id: Int,
    val content: String,
    val author: String? = null,
    val parent_id: Int? = null,
    val mentions: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val replies: List<TodoComment>? = null
)

@Serializable
data class TodoCommentsResponse(
    val success: Boolean,
    val data: List<TodoComment>
)