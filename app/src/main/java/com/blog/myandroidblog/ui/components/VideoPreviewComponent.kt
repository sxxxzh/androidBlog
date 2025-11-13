package com.blog.myandroidblog.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.compose.ui.ExperimentalComposeUiApi

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VideoPreviewComponent(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Video Player
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(videoUrl))
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.setOnVideoSizeChangedListener { _, _, _ ->
                                // Handle video size changes if needed
                            }
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                        setOnErrorListener { _, what, extra ->
                            // Handle video errors
                            false
                        }
                        setOnClickListener {
                            isPlaying = !isPlaying
                            if (isPlaying) start() else pause()
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
            
            // Click overlay for play/pause toggle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter {
                        if (it.action == MotionEvent.ACTION_UP) {
                            isPlaying = !isPlaying
                        }
                        true
                    }
            )
            
            // Play/Pause indicator (subtle, center)
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Playing" else "Paused",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
