package com.blog.myandroidblog.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.blog.myandroidblog.data.remote.AuthStore
import com.blog.myandroidblog.ui.screens.LoginScreen
import com.blog.myandroidblog.ui.screens.RegisterScreen
import com.blog.myandroidblog.ui.screens.PostListScreen
import com.blog.myandroidblog.ui.screens.EnhancedPostDetailScreen
import android.util.Log

enum class Screen {
    LOGIN,
    REGISTER,
    POST_LIST,
    POST_DETAIL
}

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.POST_LIST) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // 初始化持久化的登录信息
    LaunchedEffect(Unit) {
        AuthStore.initialize(context)
        Log.i("AuthStore", "AppRoot init isAuthenticated=${AuthStore.isAuthenticated()} user=${AuthStore.getCurrentUser()?.id}:${AuthStore.getCurrentUser()?.name}")
    }
    // 未登录也可浏览文章：默认进入文章列表，不强制跳转登录
    
    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = {
                    currentScreen = Screen.POST_LIST
                },
                onNavigateToRegister = {
                    currentScreen = Screen.REGISTER
                },
                onNavigateHome = {
                    currentScreen = Screen.POST_LIST
                },
                modifier = modifier
            )
        }
        
        Screen.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = {
                    currentScreen = Screen.LOGIN
                },
                onNavigateToLogin = {
                    currentScreen = Screen.LOGIN
                },
                modifier = modifier
            )
        }
        
        Screen.POST_LIST -> {
            PostListScreen(
                onPostClick = { postId ->
                    selectedPostId = postId
                    currentScreen = Screen.POST_DETAIL
                },
                onLogout = {
                    AuthStore.clearToken()
                    currentScreen = Screen.LOGIN
                },
                onLogin = {
                    currentScreen = Screen.LOGIN
                },
                modifier = modifier
            )
        }
        
        Screen.POST_DETAIL -> {
            selectedPostId?.let { postId ->
                EnhancedPostDetailScreen(
                    postId = postId,
                    onBack = {
                        currentScreen = Screen.POST_LIST
                    },
                    onLoginRequest = {
                        currentScreen = Screen.LOGIN
                    },
                    modifier = modifier
                )
            }
        }
    }
}
