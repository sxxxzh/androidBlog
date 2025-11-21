package com.blog.myandroidblog.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.ExperimentalComposeUiApi

/**
 * 流媒体视频预览组件（增强版，支持流式预览和错误处理，控制按钮在底部）
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamingVideoPreviewComponent(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Video player area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
            ) {
                if (hasError) {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "无法加载视频",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                setVideoURI(Uri.parse(videoUrl))
                                setOnPreparedListener { mediaPlayer ->
                                    // Auto-start when prepared
                                    start()
                                    isPlaying = true
                                    isLoading = false
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                }
                                setOnErrorListener { _, _, _ ->
                                    isPlaying = false
                                    isLoading = false
                                    hasError = true
                                    false
                                }
                                
                            }
                        },
                        update = { videoView ->
                            if (isPlaying) {
                                videoView.start()
                            } else {
                                videoView.pause()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Loading overlay (centered)
                    if (isLoading) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(56.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                // Interaction overlay inside video box
                if (!isLoading && !hasError) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { isPlaying = !isPlaying },
                                    onDoubleTap = {
                                        isPlaying = false
                                        isFullscreen = true
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
    if (isFullscreen) {
        var fullscreenLoading by remember { mutableStateOf(true) }
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var fsPlaying by remember { mutableStateOf(false) }
        var fullscreenVideoView by remember { mutableStateOf<android.widget.VideoView?>(null) }
        Dialog(onDismissRequest = { isFullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            fullscreenVideoView = this
                            setVideoURI(Uri.parse(videoUrl))
                            keepScreenOn = true
                            setOnPreparedListener {
                                fullscreenLoading = false
                                fsPlaying = true
                                start()
                            }
                            setOnErrorListener { _, _, _ ->
                                fullscreenLoading = false
                                false
                            }
                            setOnCompletionListener { fsPlaying = false }
                        }
                    },
                    update = { v ->
                        fullscreenVideoView = v
                        if (fsPlaying) v.start() else v.pause()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                offset += pan
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    fsPlaying = !fsPlaying
                                    val v = fullscreenVideoView
                                    if (v != null) {
                                        if (fsPlaying) v.start() else v.pause()
                                    }
                                },
                                onDoubleTap = { isFullscreen = false }
                            )
                        }
                )
                if (fullscreenLoading) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * 简化的视频预览卡片（使用占位符）
 */
@Composable
fun SimpleVideoPreviewCard(
    attachment: FileAttachment,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Video thumbnail placeholder
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onPlayClick,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            text = buildString {
                                append(formatFileSize(attachment.size))
                                append(" • Video")
                                if (!attachment.durationFormatted.isNullOrBlank()) {
                                    append(" • ")
                                    append(attachment.durationFormatted)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
