package com.blog.myandroidblog.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val created_at: Long? = null,
    val updated_at: Long? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: User
)

@Serializable
data class ApiError(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val details: List<String>? = null
)