package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.remote.AuthStore
import com.blog.myandroidblog.data.models.Post
import com.blog.myandroidblog.data.models.Comment
import com.blog.myandroidblog.data.models.CreateCommentRequest
import com.blog.myandroidblog.data.models.Tag
import com.blog.myandroidblog.ui.components.MarkdownRenderer
import com.blog.myandroidblog.ui.components.EnhancedMarkdownRenderer
import com.blog.myandroidblog.ui.components.FilePreviewComponent2
import com.blog.myandroidblog.ui.components.FileListComponent
import com.blog.myandroidblog.ui.components.ErrorDialog
import com.blog.myandroidblog.ui.components.SuccessDialog
import com.blog.myandroidblog.ui.components.FileAttachment
import com.blog.myandroidblog.ui.components.FileDownloadManager
import com.blog.myandroidblog.data.models.FileAsset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onLoginRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newComment by remember { mutableStateOf("") }
    var isSubmittingComment by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<FileAttachment>>(emptyList()) }
    var postTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogTitle by remember { mutableStateOf("错误") }
    var errorDialogMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var showCommentSuccess by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<com.blog.myandroidblog.data.models.User?>(null) }
    var isAuthed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        com.blog.myandroidblog.data.remote.AuthStore.initialize(context)
        isAuthed = com.blog.myandroidblog.data.remote.AuthStore.isAuthenticated()
        currentUser = com.blog.myandroidblog.data.remote.AuthStore.getCurrentUser()
    }
    
    // Safe date formatting function
    fun formatDate(timestamp: Long?): String {
        return try {
            if (timestamp != null && timestamp > 0) {
                dateFormat.format(Date(timestamp * 1000))
            } else {
                "Unknown date"
            }
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    fun loadPost() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                post = ApiService.getPost(postId)
                comments = ApiService.getComments(postId)
                val files = ApiService.getPostFiles(postId)
                attachments = files.map { toAttachment(it) }
                postTags = ApiService.getPostTags(postId)
            } catch (e: Exception) {
                errorDialogTitle = "加载失败"
                errorDialogMessage = when {
                    e.message?.contains("ConnectException") == true -> "网络连接失败，请检查网络连接。"
                    e.message?.contains("UnknownHostException") == true -> "无法连接到服务器，请检查服务器是否可用。"
                    e.message?.contains("permission") == true -> "网络权限被拒绝，请检查应用权限。"
                    else -> "加载文章失败：${e.message ?: "未知错误"}"
                }
                showErrorDialog = true
            } finally {
                isLoading = false
            }
        }
    }
    
    fun refreshPost() {
        scope.launch {
            isRefreshing = true
            try {
                post = ApiService.getPost(postId)
                comments = ApiService.getComments(postId)
                val files = ApiService.getPostFiles(postId)
                attachments = files.map { toAttachment(it) }
                postTags = ApiService.getPostTags(postId)
            } catch (e: Exception) {
                errorDialogTitle = "刷新失败"
                errorDialogMessage = "刷新内容失败：${e.message ?: "未知错误"}"
                showErrorDialog = true
            } finally {
                isRefreshing = false
            }
        }
    }
    
    fun submitComment() {
        if (newComment.isBlank()) return
        
        scope.launch {
            isSubmittingComment = true
            try {
                ApiService.addComment(postId, CreateCommentRequest(newComment))
                newComment = ""
                // Reload comments after submission
                comments = ApiService.getComments(postId)
                // Show success animation
                showCommentSuccess = true
                // Hide success animation after delay
                kotlinx.coroutines.delay(2000)
                showCommentSuccess = false
            } catch (e: Exception) {
                errorDialogTitle = "评论失败"
                errorDialogMessage = "提交评论失败：${e.message}"
                showErrorDialog = true
            } finally {
                isSubmittingComment = false
            }
        }
    }
    
    fun downloadFile(attachment: FileAttachment) {
        val downloadId = FileDownloadManager.downloadFile(
            context = context,
            fileUrl = attachment.downloadUrl ?: attachment.url,
            fileName = attachment.name,
            mimeType = attachment.mimeType
        )
        
        if (downloadId != -1L) {
            successDialogMessage = "下载已开始：${attachment.name}"
            showSuccessDialog = true
        } else {
            errorDialogTitle = "下载失败"
            errorDialogMessage = "无法开始下载：${attachment.name}"
            showErrorDialog = true
        }
    }
    
    LaunchedEffect(postId) {
        loadPost()
    }
    
    val clipboard = LocalClipboardManager.current
    Scaffold(
        modifier = modifier,
        topBar = {
                    TopAppBar(
                title = { 
                    val t = post?.title ?: "Loading..."
                    Text(if (t.length > 10) t.take(10) + "..." else t)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isAuthed) {
                        TextButton(onClick = onLoginRequest) { Text("登录") }
                    } else {
                        if ((post?.content ?: "").isNotBlank()) {
                            TextButton(onClick = { clipboard.setText(AnnotatedString(post!!.content)) }) { Text("复制") }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Error Dialog
        if (showErrorDialog) {
            ErrorDialog(
                title = errorDialogTitle,
                message = errorDialogMessage,
                onDismiss = { showErrorDialog = false },
                onRetry = if (errorDialogTitle == "加载失败") { { showErrorDialog = false; loadPost() } } else null
            )
        }
        
        // Success Dialog
        if (showSuccessDialog) {
            SuccessDialog(
                message = successDialogMessage,
                onDismiss = { showSuccessDialog = false }
            )
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            post == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "文章未找到",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            post != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Post header card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Title
                            Text(
                                text = post?.title ?: "Untitled",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Author and date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "By ${post?.author_name ?: "Unknown Author"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = formatDate(post?.created_at),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            // Status badge if not published
                            if (post?.status != "published") {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = post?.status?.uppercase() ?: "UNKNOWN",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            // Tags row
                            if (postTags.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    postTags.forEach { tag ->
                                        TagChip(label = tag.name)
                                    }
                                }
                            }
                        }
                    }
                    }
                    
                    // File Preview Section (显示在标题正下方 - 只预览图片和视频)
                    attachments.takeIf { it.isNotEmpty() }?.let { list ->
                        item {
                            AnimatedVisibility(
                                visible = list.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                FilePreviewComponent2(
                                    attachments = list,
                                    onDownloadClick = ::downloadFile,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                    
                    // Content card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                            ) {
                                Text(
                                    text = "Content",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    EnhancedMarkdownRenderer(
                                        markdown = post?.content ?: "No content available",
                                        isDarkMode = false,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                    
                    // File List Section (附件列表 - 显示在文章下方评论上方)
                    attachments.takeIf { it.isNotEmpty() }?.let { list ->
                        item {
                            AnimatedVisibility(
                                visible = list.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                            ) {
                                FileListComponent(
                                    attachments = list,
                                    onDownloadClick = ::downloadFile,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                    
                    // Comments Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Text(
                                    text = "Comments (${comments.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Add comment form (only for authenticated users)
                                if (currentUser != null) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newComment,
                                                onValueChange = { newComment = it },
                                                label = { Text("Add a comment...") },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text
                                                ),
                                                maxLines = 3,
                                                enabled = !isSubmittingComment
                                            )
                                            
                                            IconButton(
                                                onClick = { submitComment() },
                                                enabled = newComment.isNotBlank() && !isSubmittingComment,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                if (isSubmittingComment) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.Send,
                                                        contentDescription = "Send comment",
                                                        tint = if (newComment.isNotBlank()) 
                                                            MaterialTheme.colorScheme.primary 
                                                        else 
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Success animation
                                        AnimatedVisibility(
                                            visible = showCommentSuccess,
                                            enter = fadeIn() + slideInVertically(),
                                            exit = fadeOut() + slideOutVertically()
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Success",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "评论发表成功！",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Comments list or empty message
                                if (comments.isEmpty()) {
                                    Text(
                                        text = "No comments yet. Be the first to comment!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                } else {
                                    // Display comments directly in Column, not nested LazyColumn
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        comments.forEach { comment ->
                                            CommentItem(comment, dateFormat, ::formatDate)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun TagChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun toAttachment(asset: FileAsset): FileAttachment {
    val url = "${com.blog.myandroidblog.data.remote.ApiClient.BASE_URL}/api/files/${asset.id}"
    val mime = when (asset.type.lowercase()) {
        "image" -> "image/*"
        "video" -> "video/*"
        else -> "application/octet-stream"
    }
    return FileAttachment(
        id = asset.id,
        name = asset.filename,
        url = url,
        size = asset.size,
        mimeType = mime,
        downloadUrl = url,
        durationFormatted = asset.duration_formatted
    )
}

@Composable
fun CommentItem(
    comment: Comment,
    dateFormat: SimpleDateFormat,
    formatDate: (Long?) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.author_name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = formatDate(comment.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
