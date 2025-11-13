package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val created_at: Long,
    val updated_at: Long,
    val post_count: Int = 0
)

@Serializable
data class TagPostsResponse(
    val data: List<Post>,
    val pagination: Pagination
)

@Serializable
data class CreateTagRequest(
    val name: String
)

@Serializable
data class UpdateTagRequest(
    val name: String
)

@Serializable
data class TagResponse(
    val success: Boolean,
    val message: String,
    val tag: Tag? = null
)

@Serializable
data class SetPostTagsRequest(
    val tagIds: List<String>
)