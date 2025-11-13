package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.remote.AuthStore
import com.blog.myandroidblog.data.models.Post
import com.blog.myandroidblog.data.models.Tag
import com.blog.myandroidblog.ui.components.LoadingView
import com.blog.myandroidblog.ui.components.ErrorView
import com.blog.myandroidblog.ui.components.EmptyView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (String) -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var hasNextPage by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var selectedTagId by remember { mutableStateOf<String?>(null) }
    var isTagsLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentUser by remember { mutableStateOf<com.blog.myandroidblog.data.models.User?>(null) }
    var isAuthed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        com.blog.myandroidblog.data.remote.AuthStore.initialize(context)
        isAuthed = com.blog.myandroidblog.data.remote.AuthStore.isAuthenticated()
        currentUser = com.blog.myandroidblog.data.remote.AuthStore.getCurrentUser()
    }
    
    fun loadPosts(page: Int = 1, isLoadMore: Boolean = false) {
        scope.launch {
            if (!isLoadMore) {
                isLoading = true
            }
            errorMessage = null
            try {
                if (selectedTagId == null) {
                    val response = ApiService.getPosts(page = page)
                    if (isLoadMore && page > 1) {
                        posts = posts + response.data
                    } else {
                        posts = response.data
                    }
                    hasNextPage = response.pagination.hasNext
                } else {
                    val response = ApiService.getPostsByTag(tagId = selectedTagId!!, page = page)
                    if (isLoadMore && page > 1) {
                        posts = posts + response.data
                    } else {
                        posts = response.data
                    }
                    hasNextPage = response.pagination.hasNext
                }
                currentPage = page
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("ConnectException") == true -> "Network connection failed. Please check your internet connection."
                    e.message?.contains("UnknownHostException") == true -> "Unable to reach server. Please check if the server is available."
                    e.message?.contains("permission") == true -> "Network permission denied. Please check app permissions."
                    else -> "Failed to load posts: ${e.message ?: "Unknown error"}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadTags() {
        scope.launch {
            isTagsLoading = true
            try {
                tags = ApiService.getTags()
            } catch (_: Exception) {
                // ignore tag load error to avoid blocking posts
            } finally {
                isTagsLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadTags()
        loadPosts()
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("szhAoBlog") },
                actions = {
                    if (isAuthed) {
                        Box {
                            IconButton(onClick = { showUserMenu = true }) { Icon(Icons.Filled.Person, contentDescription = "Account") }
                            DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                                currentUser?.let { user ->
                                    DropdownMenuItem(
                                        text = { Column { Text(user.name, style = MaterialTheme.typography.bodyMedium); Text(user.email, style = MaterialTheme.typography.bodySmall) } },
                                        onClick = { }
                                    )
                                    HorizontalDivider()
                                }
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        showUserMenu = false
                                        onLogout()
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") }
                                )
                            }
                        }
                    } else {
                        TextButton(onClick = onLogin) { Text("登录") }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTagChip(
                        label = "全部",
                        selected = selectedTagId == null,
                        onClick = {
                            selectedTagId = null
                            hasNextPage = false
                            currentPage = 1
                            loadPosts(1)
                        }
                    )
                    tags.forEach { tag ->
                        FilterTagChip(
                            label = tag.name,
                            selected = selectedTagId == tag.id,
                            onClick = {
                                selectedTagId = tag.id
                                hasNextPage = false
                                currentPage = 1
                                loadPosts(1)
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    errorMessage != null -> {
                        ErrorView(
                            message = errorMessage!!,
                            onRetry = { loadPosts(currentPage) }
                        )
                    }
                    posts.isEmpty() && !isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyView("No posts available")
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(posts) { post ->
                                PostCard(
                                    post = post,
                                    dateFormat = dateFormat,
                                    onClick = { onPostClick(post.id) }
                                )
                            }
                            if (hasNextPage) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            onClick = { loadPosts(currentPage + 1, isLoadMore = true) },
                                            enabled = !isLoading
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text("Load More")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun stripMarkdown(text: String): String {
    var s = text
    s = s.replace(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE), "")
    s = s.replace(Regex("`+"), "")
    s = s.replace(Regex("!\\[[^]]*]\\([^)]*\\)"), "")
    s = s.replace(Regex("\\[([^]]+)\\]\\([^)]*\\)"), "$1")
    s = s.replace(Regex("\\*\\*|__|\\*|_"), "")
    s = s.replace(Regex("~~"), "")
    s = s.replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
    s = s.replace(Regex("^>\\s?", RegexOption.MULTILINE), "")
    s = s.replace(Regex("^\\s*([-*+]|\\d+\\.)\\s+", RegexOption.MULTILINE), "")
    s = s.replace("|", " ")
    s = s.replace(Regex("<[^>]+>"), "")
    s = s.replace(Regex("\\s+"), " ").trim()
    return s
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = buildString {
                    val plain = stripMarkdown(post.content)
                    append(if (plain.length > 140) plain.take(140) + "..." else plain)
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.4
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${post.author_name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = dateFormat.format(Date((post.created_at - 8 * 3600) * 1000)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Attachment and comment indicators (removed for now as Post model doesn't have these fields)
            // These can be added back when the API provides attachment and comment count data
            
            if (post.status != "published") {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = post.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTagChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (selected) 3.dp else 0.dp
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
