package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)