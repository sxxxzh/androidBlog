package com.blog.myandroidblog.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.blog.myandroidblog.data.models.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import android.util.Log

object AuthStore {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "auth_user"
    private const val TAG = "AuthStore"
    private var token: String? = null
    private var currentUser: User? = null
    private var prefs: SharedPreferences? = null
    
    private fun maskToken(t: String?): String {
        if (t.isNullOrBlank()) return "<empty>"
        return if (t.length <= 8) "***${t.length}***" else t.take(4) + "***" + t.takeLast(4)
    }
    
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            token = prefs?.getString(KEY_TOKEN, null)
            prefs?.getString(KEY_USER, null)?.let { json ->
                runCatching { Json.decodeFromString<User>(json) }
                    .onSuccess { currentUser = it }
                    .onFailure { Log.e(TAG, "initialize decode user failed", it) }
            }
            Log.d(TAG, "initialize token=${maskToken(token)} user=${currentUser?.id}:${currentUser?.name}")
        }
    }
    
    fun saveAuth(token: String, user: User) {
        this.token = token
        this.currentUser = user
        prefs?.edit()?.apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, Json.encodeToString(user))
            apply()
        }
        Log.i(TAG, "saveAuth token=${maskToken(token)} user=${user.id}:${user.name}")
    }
    
    fun saveToken(token: String) {
        this.token = token
        prefs?.edit()?.apply {
            putString(KEY_TOKEN, token)
            apply()
        }
        Log.i(TAG, "saveToken token=${maskToken(token)}")
    }
    
    fun clearToken() {
        this.token = null
        this.currentUser = null
        prefs?.edit()?.apply {
            remove(KEY_TOKEN)
            remove(KEY_USER)
            apply()
        }
        Log.i(TAG, "clearToken")
    }
    
    fun isAuthenticated(): Boolean {
        val persisted = prefs?.getString(KEY_TOKEN, null)
        val result = (token ?: persisted) != null
        Log.d(TAG, "isAuthenticated mem=${token != null} persisted=${persisted != null} result=$result")
        return result
    }
    
    fun getCurrentUser(): User? {
        if (currentUser != null) {
            Log.d(TAG, "getCurrentUser cached user=${currentUser?.id}:${currentUser?.name}")
            return currentUser
        }
        val json = prefs?.getString(KEY_USER, null) ?: run {
            Log.d(TAG, "getCurrentUser no persisted user")
            return null
        }
        val u = runCatching { Json.decodeFromString<User>(json) }
            .onFailure { Log.e(TAG, "getCurrentUser decode failed", it) }
            .getOrNull()
        if (u != null) Log.d(TAG, "getCurrentUser loaded user=${u.id}:${u.name}")
        currentUser = u
        return u
    }
    
    fun getToken(): String? {
        if (token != null) {
            Log.d(TAG, "getToken cached token=${maskToken(token)}")
            return token
        }
        token = prefs?.getString(KEY_TOKEN, null)
        Log.d(TAG, "getToken loaded token=${maskToken(token)}")
        return token
    }
}
