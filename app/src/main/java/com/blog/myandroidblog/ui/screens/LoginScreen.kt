package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.remote.AuthStore
import com.blog.myandroidblog.data.models.LoginRequest
import com.blog.myandroidblog.ui.components.ErrorDialog
import com.blog.myandroidblog.ui.components.LoadingDialog
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("登录") },
                navigationIcon = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "欢迎回来",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = { Text("邮箱") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = { Text("密码") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "请输入邮箱和密码"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    Log.d("AuthStore", "login start email=$email")
                                    val response = ApiService.login(LoginRequest(email, password))
                                    AuthStore.saveAuth(response.token, response.user)
                                    Log.i("AuthStore", "login success user=${response.user.id}:${response.user.name}")
                                    onLoginSuccess()
                                } catch (e: Exception) {
                                    Log.e("AuthStore", "login failed", e)
                                    errorMessage = when {
                                        e.message?.contains("ConnectException") == true -> "网络连接失败，请检查网络"
                                        e.message?.contains("UnknownHostException") == true -> "无法连接到服务器"
                                        e.message?.contains("permission") == true -> "网络权限被拒绝"
                                        e.message?.contains("401") == true -> "邮箱或密码错误"
                                        e.message?.contains("400") == true -> "输入不合法，请检查"
                                        else -> "登录失败：${e.message ?: "未知错误"}"
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("登录")
                    }
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("没有账号？去注册")
                    }
                }
            }
        }
        if (isLoading) {
            LoadingDialog(message = "正在登录...")
        }
        if (errorMessage != null) {
            ErrorDialog(
                title = "登录失败",
                message = errorMessage!!,
                onDismiss = { errorMessage = null }
            )
        }
    }
}
