package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FileAsset(
    val id: String,
    val filename: String,
    val path: String? = null,
    val size: Long,
    val type: String,
    val post_id: String? = null,
    val user_id: String? = null,
    val created_at: Long? = null,
    val duration: Double? = null,
    val duration_formatted: String? = null
)

@Serializable
data class FileResponse(
    val success: Boolean,
    val message: String,
    val fileId: String? = null,
    val url: String? = null
)

@Serializable
data class AssociateFileRequest(
    val postId: String?
)
