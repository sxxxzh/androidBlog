package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: Int,
    val title: String,
    val content: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val due_date: String? = null,
    val tags: String? = null,
    val author: String? = null,
    val assignee: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class TodoListResponse(
    val success: Boolean,
    val data: List<Todo>,
    val pagination: Pagination? = null
)

@Serializable
data class TodoResponse(
    val success: Boolean,
    val data: Todo
)
