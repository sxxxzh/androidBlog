package com.blog.myandroidblog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class FileAttachment(
    val id: String,
    val name: String,
    val url: String,
    val size: Long,
    val mimeType: String,
    val downloadUrl: String? = null,
    val durationFormatted: String? = null
)

@Composable
fun FilePreviewComponent(
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
            text = "Attachments",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        attachments.forEach { attachment ->
            when {
                attachment.mimeType.startsWith("image/") -> {
                    ImageAttachmentCard(
                        attachment = attachment,
                        onDownloadClick = onDownloadClick
                    )
                }
                attachment.mimeType.startsWith("video/") -> {
                    VideoAttachmentCard(
                        attachment = attachment,
                        onDownloadClick = onDownloadClick
                    )
                }
                else -> {
                    FileAttachmentCard(
                        attachment = attachment,
                        onDownloadClick = onDownloadClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageAttachmentCard(
    attachment: FileAttachment,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Image preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = attachment.url,
                    contentDescription = attachment.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatFileSize(attachment.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun VideoAttachmentCard(
    attachment: FileAttachment,
    onDownloadClick: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Video thumbnail placeholder
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        modifier = Modifier.size(32.dp),
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

@Composable
private fun FileAttachmentCard(
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
        else -> "File"
    }
}