package com.blog.myandroidblog.data.remote

import com.blog.myandroidblog.data.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

object ApiService {
    private object MemoryCache {
        private val store = mutableMapOf<String, Pair<Long, Any>>()
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> get(key: String): T? {
            val now = System.currentTimeMillis()
            val entry = store[key] ?: return null
            val (expireAt, value) = entry
            return if (expireAt > now) value as T else {
                store.remove(key)
                null
            }
        }
        fun <T : Any> put(key: String, ttlMillis: Long, value: T) {
            val expireAt = System.currentTimeMillis() + ttlMillis
            store[key] = expireAt to value
        }
        suspend fun <T : Any> getOrFetch(key: String, ttlMillis: Long, fetch: suspend () -> T): T {
            val cached: T? = get(key)
            if (cached != null) return cached
            val fresh = fetch()
            put(key, ttlMillis, fresh)
            return fresh
        }
    }
    
    // Auth endpoints
    suspend fun register(request: RegisterRequest): LoginResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun login(request: LoginRequest): LoginResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Post endpoints
    suspend fun getPosts(page: Int = 1, limit: Int = 10): PostListResponse {
        val key = "posts?page=$page&limit=$limit"
        return MemoryCache.getOrFetch(key, ttlMillis = 5 * 60_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/posts") {
                parameter("page", page)
                parameter("limit", limit)
            }.body()
        }
    }
    
    suspend fun getPost(id: String): Post {
        val key = "post:$id"
        return MemoryCache.getOrFetch(key, ttlMillis = 5 * 60_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/posts/$id").body()
        }
    }
    
    suspend fun createPost(request: CreatePostRequest): PostResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/posts") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun updatePost(id: String, request: UpdatePostRequest): PostResponse {
        return ApiClient.httpClient.put("${ApiClient.BASE_URL}/api/posts/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun deletePost(id: String): ApiResponse {
        return ApiClient.httpClient.delete("${ApiClient.BASE_URL}/api/posts/$id") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun adminGetPosts(page: Int = 1, limit: Int = 10): PostListResponse {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/admin/posts") {
            parameter("page", page)
            parameter("limit", limit)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    // Tag endpoints
    suspend fun getTags(): List<Tag> {
        val key = "tags"
        return MemoryCache.getOrFetch(key, ttlMillis = 30 * 60_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/tags").body()
        }
    }
    
    suspend fun getPostsByTag(tagId: String, page: Int = 1, limit: Int = 10): TagPostsResponse {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/tags/$tagId/posts") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }
    
    suspend fun getPostTags(postId: String): List<Tag> {
        val key = "post:$postId:tags"
        return MemoryCache.getOrFetch(key, ttlMillis = 30 * 60_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/posts/$postId/tags").body()
        }
    }
    
    suspend fun createTag(request: CreateTagRequest): TagResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/tags") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun updateTag(id: String, request: UpdateTagRequest): TagResponse {
        return ApiClient.httpClient.put("${ApiClient.BASE_URL}/api/tags/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun deleteTag(id: String): ApiResponse {
        return ApiClient.httpClient.delete("${ApiClient.BASE_URL}/api/tags/$id") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun setPostTags(postId: String, request: SetPostTagsRequest): TagResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/posts/$postId/tags") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    // Comment endpoints
    suspend fun getComments(postId: String): List<Comment> {
        val key = "post:$postId:comments"
        return MemoryCache.getOrFetch(key, ttlMillis = 90_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/posts/$postId/comments").body()
        }
    }
    
    suspend fun addComment(postId: String, request: CreateCommentRequest): CommentResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/posts/$postId/comments") {
            contentType(ContentType.Application.Json)
            setBody(request)
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun adminGetComments(): List<Comment> {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/admin/comments") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun deleteComment(id: String): ApiResponse {
        return ApiClient.httpClient.delete("${ApiClient.BASE_URL}/api/comments/$id") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    // File endpoints
    suspend fun uploadFile(
        file: ByteArray,
        filename: String,
        contentType: String,
        postId: String? = null,
        duration: Int? = null
    ): FileResponse {
        return ApiClient.httpClient.submitFormWithBinaryData(
            url = "${ApiClient.BASE_URL}/api/upload",
            formData = formData {
                append("file", file, Headers.build {
                    append(HttpHeaders.ContentType, contentType)
                    append(HttpHeaders.ContentDisposition, "filename=$filename")
                })
                postId?.let { append("postId", it) }
                duration?.let { append("duration", it.toString()) }
            }
        ) {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun deleteFile(id: String): ApiResponse {
        return ApiClient.httpClient.delete("${ApiClient.BASE_URL}/api/files/$id") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun associateFileToPost(fileId: String, postId: String?): FileResponse {
        return ApiClient.httpClient.put("${ApiClient.BASE_URL}/api/files/$fileId/post") {
            contentType(ContentType.Application.Json)
            setBody(AssociateFileRequest(postId))
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    suspend fun getPostFiles(postId: String): List<FileAsset> {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/posts/$postId/files").body()
    }
    
    // Message endpoints
    suspend fun getMessages(): List<Message> {
        return MemoryCache.getOrFetch("messages", ttlMillis = 60_000) {
            ApiClient.httpClient.get("${ApiClient.BASE_URL}/api/messages").body()
        }
    }
    
    suspend fun createMessage(content: String, imageBytes: ByteArray? = null): MessageResponse {
        return if (imageBytes != null) {
            ApiClient.httpClient.submitFormWithBinaryData(
                url = "${ApiClient.BASE_URL}/api/messages",
                formData = formData {
                    append("content", content)
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=message_image.jpg")
                    })
                }
            ) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
                }
            }.body()
        } else {
            ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/messages") {
                contentType(ContentType.Application.Json)
                setBody(CreateMessageRequest(content))
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
                }
            }.body()
        }
    }
    
    suspend fun deleteMessage(id: String): ApiResponse {
        return ApiClient.httpClient.delete("${ApiClient.BASE_URL}/api/messages/$id") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${AuthStore.getToken() ?: ""}")
            }
        }.body()
    }
    
    // Utility endpoints
    suspend fun verifyTurnstile(token: String): ApiResponse {
        return ApiClient.httpClient.post("${ApiClient.BASE_URL}/api/verify-turnstile") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token))
        }.body()
    }
    
    suspend fun getLatestVersion(): VersionLatestResponse {
        return MemoryCache.getOrFetch("latestVersion", ttlMillis = 60 * 60_000) {
            ApiClient.httpClient.get("https://android.szhaovo.cn/api/versions/latest").body()
        }
    }

    suspend fun checkUpdate(currentVersionCode: Int): VersionCheckResponse {
        return ApiClient.httpClient.get("https://android.szhaovo.cn/api/versions/check-update/$currentVersionCode").body()
    }

    suspend fun getHealthCheck(): Map<String, String> {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/").body()
    }

    // Todo endpoints
    suspend fun getTodos(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        priority: String? = null,
        search: String? = null
    ): TodoListResponse {
        val key = "todos?page=$page&limit=$limit&status=${status ?: ""}&priority=${priority ?: ""}&search=${search ?: ""}"
        return MemoryCache.getOrFetch(key, ttlMillis = 90_000) {
            ApiClient.httpClient.get("https://android.szhaovo.cn/api/todos") {
                parameter("page", page)
                parameter("limit", limit)
                status?.let { parameter("status", it) }
                priority?.let { parameter("priority", it) }
                search?.let { parameter("search", it) }
            }.body()
        }
    }

    suspend fun getTodo(id: Int): TodoResponse {
        return MemoryCache.getOrFetch("todo:$id", ttlMillis = 90_000) {
            ApiClient.httpClient.get("https://android.szhaovo.cn/api/todos/$id").body()
        }
    }

    suspend fun getTodoComments(todoId: Int): TodoCommentsResponse {
        return MemoryCache.getOrFetch("todo:$todoId:comments", ttlMillis = 60_000) {
            ApiClient.httpClient.get("https://android.szhaovo.cn/api/todos/$todoId/comments").body()
        }
    }

    @kotlinx.serialization.Serializable
    data class CreateTodoCommentRequest(
        val content: String,
        val parent_id: Int? = null,
        val mentions: String? = null
    )

    @kotlinx.serialization.Serializable
    data class TodoCommentResponse(
        val success: Boolean,
        val data: com.blog.myandroidblog.data.models.TodoComment
    )

    suspend fun createTodoComment(todoId: Int, req: CreateTodoCommentRequest): TodoCommentResponse {
        return ApiClient.httpClient.post("https://android.szhaovo.cn/api/todos/$todoId/comments") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(req)
            headers {
                val token = com.blog.myandroidblog.data.remote.AuthStore.getToken()
                if (!token.isNullOrBlank()) append(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
}
