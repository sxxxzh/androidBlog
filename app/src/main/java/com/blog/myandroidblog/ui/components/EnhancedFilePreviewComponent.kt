package com.blog.myandroidblog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blog.myandroidblog.ui.components.StreamingVideoPreviewComponent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 文件预览组件 - 只显示图片和视频预览（用于标题下方）
 */
@Composable
fun FilePreviewComponent2(
    attachments: List<FileAttachment>,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val previewAttachments = attachments.filter { 
        it.mimeType.startsWith("image/") || it.mimeType.startsWith("video/")
    }
    
    if (previewAttachments.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "媒体预览",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        previewAttachments.forEach { attachment ->
            when {
                attachment.mimeType.startsWith("image/") -> {
                    ImagePreviewCard(
                        attachment = attachment,
                        onDownloadClick = onDownloadClick
                    )
                }
                attachment.mimeType.startsWith("video/") -> {
                    StreamingVideoPreviewCard(
                        attachment = attachment,
                        onDownloadClick = onDownloadClick
                    )
                }
            }
        }
    }
}

/**
 * 文件列表组件 - 显示所有文件（用于底部）
 */
@Composable
fun FileListComponent(
    attachments: List<FileAttachment>,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "附件列表",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        attachments.forEach { attachment ->
            FileAttachmentItem(
                attachment = attachment,
                onDownloadClick = onDownloadClick
            )
        }
    }
}

@Composable
private fun ImagePreviewCard(
    attachment: FileAttachment,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullscreen by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Image preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { showFullscreen = true })
                        }
                ) {
                    AsyncImage(
                        model = attachment.url,
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                TextButton(
                    onClick = { onDownloadClick(attachment) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "下载",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    if (showFullscreen) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        Dialog(onDismissRequest = { showFullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        offset += pan
                    }
                }
            ) {
                AsyncImage(
                    model = attachment.url,
                    contentDescription = attachment.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { showFullscreen = false })
                        }
                )
            }
        }
    }
}

@Composable
private fun StreamingVideoPreviewCard(
    attachment: FileAttachment,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Streaming video preview
            StreamingVideoPreviewComponent(
                videoUrl = attachment.url,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                TextButton(
                    onClick = { onDownloadClick(attachment) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "下载",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FileAttachmentItem(
    attachment: FileAttachment,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val fileIcon = when {
        attachment.mimeType.contains("pdf") -> Icons.Default.Info
        attachment.mimeType.contains("word") || attachment.name.endsWith(".doc", true) || attachment.name.endsWith(".docx", true) -> Icons.AutoMirrored.Filled.List
        attachment.mimeType.contains("excel") || attachment.name.endsWith(".xls", true) || attachment.name.endsWith(".xlsx", true) -> Icons.AutoMirrored.Filled.List
        attachment.mimeType.contains("zip") || attachment.name.endsWith(".zip", true) || attachment.name.endsWith(".rar", true) -> Icons.Default.Info
        else -> Icons.Default.Info
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = "File",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${formatFileSize(attachment.size)} • ${getFileType(attachment.mimeType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            TextButton(
                onClick = { onDownloadClick(attachment) },
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "下载",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
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

private fun getFileType(mimeType: String): String {
    return when {
        mimeType.contains("pdf") -> "PDF"
        mimeType.contains("word") || mimeType.contains("document") -> "Document"
        mimeType.contains("excel") || mimeType.contains("spreadsheet") -> "Spreadsheet"
        mimeType.contains("powerpoint") || mimeType.contains("presentation") -> "Presentation"
        mimeType.contains("zip") || mimeType.contains("archive") -> "Archive"
        mimeType.contains("text") -> "Text"
        mimeType.startsWith("image/") -> "Image"
        mimeType.startsWith("video/") -> "Video"
        else -> "File"
    }
}