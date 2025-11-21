package com.blog.myandroidblog.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton

@Composable
fun MoreWebScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val lastLoadByUrl = remember { mutableStateMapOf<String, Long>() }
    val ttlMillis = 5 * 60_000
    val sites = remember {
        listOf(
            "https://game.szhaovo.cn" to "博主的游戏网站"
        )
    }
    Column(modifier = modifier.fillMaxSize().padding(top = 48.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (selectedUrl == null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("精选站点", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "从列表中选择站点，点击卡片即可在内置浏览器打开。为提升体验，页面开启缓存并支持返回/刷新。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(4.dp))
            sites.forEachIndexed { _, (url, title) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp).clickable { selectedUrl = url }, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val icon = Icons.Filled.Info
                        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                            Text(url, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                    } else {
                        selectedUrl = null
                        webView = null
                    }
                }) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
                IconButton(onClick = {
                    webView?.reload()
                    selectedUrl?.let { lastLoadByUrl[it] = System.currentTimeMillis() }
                }) { Icon(Icons.Filled.Refresh, contentDescription = "刷新") }
            }
            AndroidView(
                factory = {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                try {
                                    com.blog.myandroidblog.data.remote.AuthStore.initialize(context)
                                    val token = com.blog.myandroidblog.data.remote.AuthStore.getToken()
                                    if (token != null && url.contains("newchat.szhaovo.cn")) {
                                        val js = """
                                            try {
                                                localStorage.setItem('chat_token', '$token');
                                                var i = document.getElementById('token-input');
                                                if (i) { i.value = '$token'; }
                                                var b = document.getElementById('login-btn');
                                                if (b) { b.click(); }
                                            } catch(e) {}
                                        """.trimIndent()
                                        view.evaluateJavascript(js, null)
                                    }
                                } catch (_: Exception) {}
                                super.onPageFinished(view, url)
                            }
                        }
                        val now = System.currentTimeMillis()
                        val last = lastLoadByUrl[selectedUrl!!] ?: 0L
                        if (now - last > ttlMillis) {
                            loadUrl(selectedUrl!!)
                            lastLoadByUrl[selectedUrl!!] = now
                        }
                        webView = this
                    }
                },
                update = { wv ->
                    val url = selectedUrl
                    if (url == null) return@AndroidView
                    val now = System.currentTimeMillis()
                    val last = lastLoadByUrl[url] ?: 0L
                    if (now - last > ttlMillis) {
                        wv.loadUrl(url)
                        lastLoadByUrl[url] = now
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    BackHandler(enabled = selectedUrl != null && (webView?.canGoBack() == true)) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        }
    }
    BackHandler(enabled = selectedUrl != null && (webView?.canGoBack() != true)) {
        selectedUrl = null
        webView = null
    }
}
