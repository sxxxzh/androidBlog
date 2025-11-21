package com.blog.myandroidblog.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.blog.myandroidblog.data.remote.AuthStore
import com.blog.myandroidblog.ui.screens.LoginScreen
import com.blog.myandroidblog.ui.screens.RegisterScreen
import com.blog.myandroidblog.ui.screens.PostListScreen
import com.blog.myandroidblog.ui.screens.EnhancedPostDetailScreen
import com.blog.myandroidblog.ui.screens.TodoListScreen
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart

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
    var currentTab by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        AuthStore.initialize(context)
        Log.i("AuthStore", "AppRoot init isAuthenticated=${AuthStore.isAuthenticated()} user=${AuthStore.getCurrentUser()?.id}:${AuthStore.getCurrentUser()?.name}")
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = {
                        currentTab = 0
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "主页") },
                    label = { Text("主页") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        currentTab = 1
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "待办") },
                    label = { Text("待办") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = {
                        currentTab = 2
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    },
                    icon = { Icon(Icons.Filled.Email, contentDescription = "聊天") },
                    label = { Text("聊天") }
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = {
                        currentTab = 3
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "更多") },
                    label = { Text("更多") }
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = {
                        currentTab = 4
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                    label = { Text("设置") }
                )
            }
        }
    ) { padding ->
        BackHandler(true) {
            val activity = context as? android.app.Activity
            when (currentScreen) {
                Screen.POST_DETAIL -> {
                    currentScreen = Screen.POST_LIST
                    selectedPostId = null
                }
                Screen.POST_LIST -> {
                    if (currentTab != 0) {
                        currentTab = 0
                        currentScreen = Screen.POST_LIST
                        selectedPostId = null
                    } else {
                        activity?.finish()
                    }
                }
                Screen.LOGIN, Screen.REGISTER -> {
                    currentScreen = Screen.POST_LIST
                }
            }
        }
        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { currentScreen = Screen.POST_LIST },
                    onNavigateToRegister = { currentScreen = Screen.REGISTER },
                    onNavigateHome = { currentScreen = Screen.POST_LIST },
                    modifier = Modifier.padding(padding)
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    onRegisterSuccess = { currentScreen = Screen.LOGIN },
                    onNavigateToLogin = { currentScreen = Screen.LOGIN },
                    modifier = Modifier.padding(padding)
                )
            }
            Screen.POST_LIST -> {
                if (currentTab == 0) {
                    PostListScreen(
                        onPostClick = { postId ->
                            selectedPostId = postId
                            currentScreen = Screen.POST_DETAIL
                        },
                        onLogout = {
                            AuthStore.clearToken()
                            currentScreen = Screen.LOGIN
                        },
                        onLogin = { currentScreen = Screen.LOGIN },
                        modifier = Modifier.padding(padding)
                    )
                } else if (currentTab == 1) {
                    TodoListScreen(modifier = Modifier.padding(padding))
                } else if (currentTab == 2) {
                    com.blog.myandroidblog.ui.screens.ChatHomeWebScreen(modifier = Modifier.padding(padding))
                } else if (currentTab == 3) {
                    com.blog.myandroidblog.ui.screens.MoreWebScreen(modifier = Modifier.padding(padding))
                } else {
                    com.blog.myandroidblog.ui.screens.NewFeaturesScreen(modifier = Modifier.padding(padding))
                }
            }
            Screen.POST_DETAIL -> {
                selectedPostId?.let { postId ->
                    EnhancedPostDetailScreen(
                        postId = postId,
                        onBack = {
                            currentScreen = Screen.POST_LIST
                            selectedPostId = null
                        },
                        onLoginRequest = { currentScreen = Screen.LOGIN },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
