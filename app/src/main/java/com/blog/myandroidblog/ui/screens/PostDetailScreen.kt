package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.models.Post
import com.blog.myandroidblog.data.models.Comment
import com.blog.myandroidblog.data.models.CreateCommentRequest
import com.blog.myandroidblog.ui.components.LoadingView
import com.blog.myandroidblog.ui.components.ErrorView
import com.blog.myandroidblog.ui.components.EmptyView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmittingComment by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    fun loadPost() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                post = ApiService.getPost(postId)
                comments = ApiService.getComments(postId)
            } catch (e: Exception) {
                errorMessage = "Failed to load post: ${e.message}"
            } finally {
                isLoading = false
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
                // Reload comments
                comments = ApiService.getComments(postId)
            } catch (e: Exception) {
                errorMessage = "Failed to add comment: ${e.message}"
            } finally {
                isSubmittingComment = false
            }
        }
    }
    
    LaunchedEffect(postId) {
        loadPost()
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Post Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                LoadingView()
            }
            errorMessage != null -> {
                ErrorView(
                    message = errorMessage!!,
                    onRetry = { loadPost() }
                )
            }
            post == null -> {
                EmptyView("Post not found")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = post!!.title,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "By ${post!!.author_name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = dateFormat.format(Date(post!!.created_at * 1000)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            if (post!!.status != "published") {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = post!!.status.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            
                            HorizontalDivider()
                            
                            Text(
                                text = post!!.content,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Justify
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            HorizontalDivider()
                            
                            Text(
                                text = "Comments (${comments.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newComment,
                                    onValueChange = { newComment = it },
                                    label = { Text("Add a comment") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                
                                Button(
                                    onClick = { submitComment() },
                                    enabled = newComment.isNotBlank() && !isSubmittingComment,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    if (isSubmittingComment) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Submit")
                                    }
                                }
                            }
                        }
                    }
                    
                    items(comments) { comment ->
                        CommentCard(
                            comment = comment,
                            dateFormat = dateFormat
                        )
                    }
                    
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentCard(
    comment: Comment,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    text = dateFormat.format(Date(comment.created_at * 1000)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}