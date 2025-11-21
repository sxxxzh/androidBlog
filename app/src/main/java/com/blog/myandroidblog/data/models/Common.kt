package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val pages: Int? = null,
    val totalPages: Int? = null,
    val hasNext: Boolean? = null,
    val hasPrev: Boolean? = null
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)
