package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.models.VersionInfo
import kotlinx.coroutines.launch

@Composable
fun NewFeaturesScreen(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var checkingUpdate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var pendingVersion by remember { mutableStateOf<VersionInfo?>(null) }
    var updateErrorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("设置", style = MaterialTheme.typography.titleMedium)
            Text("当前版本：${getCurrentVersionName(context)} (${getCurrentVersionCode(context)})", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val ctx = context
                    scope.launch {
                        try {
                            checkingUpdate = true
                            val currentCode = getCurrentVersionCode(ctx)
                            val resp = ApiService.checkUpdate(currentCode)
                            val info = resp.data?.latestVersion
                            val currentName = getCurrentVersionName(ctx)
                            val clientCodeNorm = parseVersionCodeFromName(currentName)
                            val latestCode = info?.version_code ?: 0
                            val newerByCode = latestCode > clientCodeNorm
                            val newerByName = (info?.version_name ?: "") != currentName
                            val hasUpdate = (newerByCode || newerByName) && info != null
                            checkingUpdate = false
                            if (hasUpdate) {
                                if (info!!.is_force_update == true) {
                                    downloadAndInstall(ctx, info)
                                } else {
                                    pendingVersion = info
                                    showUpdateDialog = true
                                    updateErrorMessage = null
                                }
                            } else {
                                pendingVersion = null
                                showUpdateDialog = true
                                updateErrorMessage = null
                            }
                        } catch (e: Exception) {
                            checkingUpdate = false
                            updateErrorMessage = "检查更新失败：${e.message ?: "网络错误"}"
                            showUpdateDialog = false
                        }
                    }
                }) { Text("检查更新") }
            }
        }
    }

    if (checkingUpdate) {
        com.blog.myandroidblog.ui.components.LoadingDialog(message = "正在检查更新...")
    }
    if (showUpdateDialog) {
        val info = pendingVersion
        if (info != null) {
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                title = { Text("发现新版本 ${info.version_name}") },
                text = { Text("当前版本：${getCurrentVersionName(context)}\n" + (info.release_notes ?: "是否更新到最新版本？")) },
                confirmButton = {
                    TextButton(onClick = {
                        showUpdateDialog = false
                        downloadAndInstall(context, info)
                    }) { Text("更新") }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateDialog = false }) { Text("取消") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                title = { Text("已是最新版本") },
                text = { Text("当前版本：${getCurrentVersionName(context)}，无需更新") },
                confirmButton = { TextButton(onClick = { showUpdateDialog = false }) { Text("确定") } }
            )
        }
    }
    if (updateErrorMessage != null) {
        com.blog.myandroidblog.ui.components.ErrorDialog(
            title = "检查更新失败",
            message = updateErrorMessage!!,
            onDismiss = { updateErrorMessage = null }
        )
    }
}

private fun getCurrentVersionCode(ctx: android.content.Context): Int {
    return try {
        val pm = ctx.packageManager
        val pkg = ctx.packageName
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0)).longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0).longVersionCode.toInt()
        }
    } catch (_: Exception) {
        1
    }
}

private fun getCurrentVersionName(ctx: android.content.Context): String {
    return try {
        val pm = ctx.packageManager
        val pkg = ctx.packageName
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName ?: ""
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0).versionName ?: ""
        }
    } catch (_: Exception) {
        ""
    }
}

private fun parseVersionCodeFromName(name: String): Int {
    return try {
        val parts = name.trim().removePrefix("v").split('.')
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        major * 10000 + minor * 100 + patch
    } catch (_: Exception) { 0 }
}

private fun downloadAndInstall(ctx: android.content.Context, info: VersionInfo) {
    val fileName = "szhBlog-${info.version_name}.apk"
    val request = android.app.DownloadManager.Request(android.net.Uri.parse(info.apk_url)).apply {
        setTitle("下载新版本")
        setDescription("正在下载 $fileName")
        setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
        setMimeType("application/vnd.android.package-archive")
    }
    val dm = ctx.getSystemService(android.app.DownloadManager::class.java)
    val id = dm.enqueue(request)
    val receiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            val completeId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (completeId == id) {
                val file = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val apk = java.io.File(file, fileName)
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", apk)
                val install = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(install)
            }
        }
    }
    ctx.registerReceiver(receiver, android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}
