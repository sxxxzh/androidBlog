package com.blog.myandroidblog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.*
import com.blog.myandroidblog.data.remote.ApiService
import com.blog.myandroidblog.data.models.Todo
import kotlinx.coroutines.launch

@Composable
fun TodoListScreen(modifier: Modifier = Modifier) {
    var todos by remember { mutableStateOf<List<Todo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1) }
    var hasNext by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var selectedTodo by remember { mutableStateOf<Todo?>(null) }
    var showDetail by remember { mutableStateOf(false) }
    var localTodos by remember { mutableStateOf<List<Todo>>(emptyList()) }
    var newLocalTitle by remember { mutableStateOf("") }
    var newLocalStatus by remember { mutableStateOf("pending") }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var comments by remember { mutableStateOf<List<com.blog.myandroidblog.data.models.TodoComment>>(emptyList()) }
    var loadingComments by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var commentError by remember { mutableStateOf<String?>(null) }
    var replyParentId by remember { mutableStateOf<Int?>(null) }
    var suggestionOpen by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var replyTargetAuthor by remember { mutableStateOf<String?>(null) }
    var localFilter by remember { mutableStateOf<String?>(null) }
    var showRemote by remember { mutableStateOf(true) }
    var showLocal by remember { mutableStateOf(false) }

    fun loadTodos(p: Int = 1) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val resp = ApiService.getTodos(p, 10, status, priority, search.ifBlank { null })
                todos = resp.data
                val p = resp.pagination
                hasNext = p?.hasNext ?: ((p?.totalPages ?: p?.pages ?: 1) > (p?.page ?: 1))
                page = p?.page ?: 1
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadLocalTodos() {
        val sp = ctx.getSharedPreferences("local_todos", android.content.Context.MODE_PRIVATE)
        val raw = sp.getString("items", null)
        localTodos = runCatching { if (!raw.isNullOrBlank()) Json.decodeFromString<List<Todo>>(raw) else emptyList() }.getOrDefault(emptyList())
    }

    fun saveLocalTodos() {
        val sp = ctx.getSharedPreferences("local_todos", android.content.Context.MODE_PRIVATE)
        sp.edit().putString("items", Json.encodeToString(localTodos)).apply()
    }

    fun addLocalTodo() {
        if (newLocalTitle.isBlank()) return
        val idBase = (localTodos.maxOfOrNull { it.id } ?: 0) + 1
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
        val t = Todo(id = idBase, title = newLocalTitle, status = newLocalStatus, author = "本地", created_at = sdf.format(Date()))
        localTodos = listOf(t) + localTodos
        newLocalTitle = ""
        saveLocalTodos()
    }

    fun updateLocalStatus(id: Int, status: String) {
        localTodos = localTodos.map { if (it.id == id) it.copy(status = status) else it }
        saveLocalTodos()
    }

    fun editLocalTodoTitle(id: Int, newTitle: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
        localTodos = localTodos.map { if (it.id == id) it.copy(title = newTitle, updated_at = sdf.format(Date())) else it }
        saveLocalTodos()
    }

    fun deleteLocalTodo(id: Int) {
        localTodos = localTodos.filterNot { it.id == id }
        saveLocalTodos()
    }

    LaunchedEffect(Unit) { loadTodos(); loadLocalTodos() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("博主待办", style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = { showRemote = !showRemote }) { Text(if (showRemote) "收起" else "展开") }
        }
        if (showRemote) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("搜索") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = { loadTodos(1) }) { Icon(Icons.Filled.Search, contentDescription = "查询") }
                }
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            WideFilterChip(label = "全部", selected = status == null, modifier = Modifier.weight(1f)) { status = null; loadTodos(1) }
            WideFilterChip(label = "待办", selected = status == "pending", modifier = Modifier.weight(1f)) { status = "pending"; loadTodos(1) }
            WideFilterChip(label = "进行中", selected = status == "in_progress", modifier = Modifier.weight(1f)) { status = "in_progress"; loadTodos(1) }
            WideFilterChip(label = "已完成", selected = status == "completed", modifier = Modifier.weight(1f)) { status = "completed"; loadTodos(1) }
        }
        Spacer(Modifier.height(8.dp))
        if (errorMessage != null) {
            Text("加载失败：$errorMessage", color = MaterialTheme.colorScheme.error)
        }
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("正在加载...", style = MaterialTheme.typography.bodySmall)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(todos) { todo ->
                Box(modifier = Modifier.clickable {
                    selectedTodo = todo
                    showDetail = true
                }) {
                    TodoCard(todo)
                }
            }
            if (hasNext) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(onClick = { loadTodos(page + 1) }) { Text("加载更多") }
                    }
                }
            }
        }
        }
        // 我的待办创建与列表（放置到下方）
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("我的待办", style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = { showLocal = !showLocal }) { Text(if (showLocal) "收起" else "展开") }
        }
        if (showLocal) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newLocalTitle,
                        onValueChange = { newLocalTitle = it },
                        label = { Text("添加我的待办") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = { IconButton(onClick = { addLocalTodo() }) { Icon(Icons.Filled.Add, contentDescription = "创建") } }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    WideFilterChip(label = "全部", selected = localFilter == null, modifier = Modifier.weight(1f)) { localFilter = null }
                    WideFilterChip(label = "待办", selected = localFilter == "pending", modifier = Modifier.weight(1f)) { localFilter = "pending" }
                    WideFilterChip(label = "进行中", selected = localFilter == "in_progress", modifier = Modifier.weight(1f)) { localFilter = "in_progress" }
                    WideFilterChip(label = "已完成", selected = localFilter == "completed", modifier = Modifier.weight(1f)) { localFilter = "completed" }
                }
            }
            val displayLocal = localTodos.filter { localFilter?.let { f -> (it.status ?: "") == f } ?: true }
            if (displayLocal.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(displayLocal) { todo ->
                        LocalTodoCard(
                            todo,
                            onSetStatus = { s -> updateLocalStatus(todo.id, s) },
                            onEditTitle = { newTitle -> editLocalTodoTitle(todo.id, newTitle) },
                            onDelete = { deleteLocalTodo(todo.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDetail && selectedTodo != null) {
        val t = selectedTodo!!
        val ctx = androidx.compose.ui.platform.LocalContext.current
        var isAuthed by remember { mutableStateOf(false) }
        var showLoginPrompt by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            com.blog.myandroidblog.data.remote.AuthStore.initialize(ctx)
            isAuthed = com.blog.myandroidblog.data.remote.AuthStore.isAuthenticated()
        }
        LaunchedEffect(t.id) {
            loadingComments = true
            commentError = null
            try {
                comments = ApiService.getTodoComments(t.id).data
            } catch (e: Exception) {
                commentError = e.message
            } finally {
                loadingComments = false
            }
        }
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = { Text(t.title) },
            text = {
                val contentScroll = rememberScrollState()
                val commentsScroll = rememberScrollState()
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!t.status.isNullOrBlank()) {
                            val (chipBg, chipFg) = statusChipColors(t.status, MaterialTheme.colorScheme)
                            Surface(shape = MaterialTheme.shapes.small, color = chipBg) {
                                Text(statusText(t.status!!), style = MaterialTheme.typography.labelSmall, color = chipFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        if (!t.priority.isNullOrBlank()) {
                            val (pBg, pFg) = priorityChipColors(t.priority, MaterialTheme.colorScheme)
                            Surface(shape = MaterialTheme.shapes.small, color = pBg) {
                                Text(priorityText(t.priority!!), style = MaterialTheme.typography.labelSmall, color = pFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        if (!t.due_date.isNullOrBlank()) {
                            Text("截止：${t.due_date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!t.author.isNullOrBlank()) Text("作者：${t.author}", style = MaterialTheme.typography.bodySmall)
                        if (!t.assignee.isNullOrBlank()) Text("负责人：${t.assignee}", style = MaterialTheme.typography.bodySmall)
                        val tags = parseTags(t.tags)
                        if (tags.isNotEmpty()) {
                            tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), contentAlignment = Alignment.Center) {
                                        Text(tag, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                    if (!t.content.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp).verticalScroll(contentScroll)) {
                            Text(text = t.content!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (!t.created_at.isNullOrBlank()) Text("创建：${t.created_at}", style = MaterialTheme.typography.bodySmall)
                        if (!t.updated_at.isNullOrBlank()) Text("更新：${t.updated_at}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("评论", style = MaterialTheme.typography.labelMedium)
                        Text("${comments.size} 条", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    if (loadingComments) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("正在加载评论...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (commentError != null) {
                        Text("评论加载失败：$commentError", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        if (comments.isEmpty()) {
                            Text("暂无评论", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(commentsScroll)) {
                                CommentsTree(comments = comments, onReply = { c ->
                                    replyParentId = c.id
                                    replyTargetAuthor = c.author
                                })
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CompactTextField(
                            value = newComment,
                            onValueChange = { txt ->
                                newComment = txt
                                val atIdx = txt.lastIndexOf('@')
                                if (atIdx >= 0) {
                                    val query = txt.substring(atIdx + 1).takeWhile { it.isLetterOrDigit() || it == '_' }
                                    val pool = buildSuggestionBase(t, comments)
                                    suggestions = pool.filter { it.lowercase().startsWith(query.lowercase()) }.take(6)
                                    suggestionOpen = suggestions.isNotEmpty()
                                } else {
                                    suggestionOpen = false
                                }
                            },
                            label = if (!isAuthed) "请先登录后评论" else if (replyParentId != null) "回复${replyTargetAuthor ?: ""}" else "添加评论",
                            modifier = Modifier.fillMaxWidth(),
                            onSubmit = {
                                scope.launch {
                                    if (newComment.isBlank()) return@launch
                                    if (!isAuthed) {
                                        showLoginPrompt = true
                                        return@launch
                                    }
                                    try {
                                        commentError = null
                                val resp = ApiService.createTodoComment(
                                    t.id,
                                    ApiService.CreateTodoCommentRequest(
                                        content = newComment,
                                        parent_id = replyParentId,
                                        mentions = encodeMentionsJson(parseMentionsFromText(newComment))
                                    )
                                )
                                newComment = ""
                                comments = ApiService.getTodoComments(t.id).data
                                replyParentId = null
                                replyTargetAuthor = null
                            } catch (e: Exception) {
                                commentError = e.message
                            }
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = suggestionOpen,
                            onDismissRequest = { suggestionOpen = false },
                            offset = androidx.compose.ui.unit.DpOffset(0.dp, (-160).dp)
                        ) {
                            suggestions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("@$s", style = MaterialTheme.typography.labelSmall) },
                                    onClick = {
                                        newComment = replaceLastMention(newComment, s)
                                        suggestionOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp), modifier = Modifier.height(20.dp)) { Text("关闭", style = MaterialTheme.typography.labelSmall) }
            }
        )
        if (showLoginPrompt) {
            AlertDialog(
                onDismissRequest = { showLoginPrompt = false },
                title = { Text("请先登录") },
                text = { Text("登录后才能发表评论") },
                confirmButton = { TextButton(onClick = { showLoginPrompt = false }) { Text("确定") } }
            )
        }
    }

}

@Composable
private fun WideFilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LocalTodoCard(todo: Todo, onSetStatus: (String) -> Unit, onEditTitle: (String) -> Unit, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (cardBg, cardFg) = statusColors(todo.status, scheme)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            var editing by remember { mutableStateOf(false) }
            var draftTitle by remember { mutableStateOf(todo.title) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (editing) {
                    OutlinedTextField(value = draftTitle, onValueChange = { draftTitle = it }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = {
                        if (draftTitle.isNotBlank()) onEditTitle(draftTitle)
                        editing = false
                    }) { Icon(Icons.Filled.Check, contentDescription = "保存") }
                    IconButton(onClick = { editing = false }) { Icon(Icons.Filled.Close, contentDescription = "取消") }
                } else {
                    Text(text = todo.title, style = MaterialTheme.typography.titleSmall, color = cardFg, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    val (chipBg, chipFg) = statusChipColors(todo.status, scheme)
                    Surface(shape = MaterialTheme.shapes.small, color = chipBg) { Text(statusText(todo.status ?: ""), style = MaterialTheme.typography.labelSmall, color = chipFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                    IconButton(onClick = { editing = true }, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp)) }
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiniFilterChip(label = "待办", selected = (todo.status ?: "") == "pending", statusKey = "pending") { onSetStatus("pending") }
                    MiniFilterChip(label = "进行中", selected = (todo.status ?: "") == "in_progress", statusKey = "in_progress") { onSetStatus("in_progress") }
                    MiniFilterChip(label = "已完成", selected = (todo.status ?: "") == "completed", statusKey = "completed") { onSetStatus("completed") }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                    if (!todo.created_at.isNullOrBlank()) { Text(text = "创建：${todo.created_at}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
                    if (!todo.updated_at.isNullOrBlank()) { Text(text = "更新：${todo.updated_at}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                }
            }
        }
    }
}

@Composable
private fun MiniFilterChip(label: String, selected: Boolean, statusKey: String? = null, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = if (statusKey != null && selected) statusChipColors(statusKey, scheme) else (
        if (selected) scheme.primaryContainer to scheme.onPrimaryContainer else scheme.surfaceVariant to scheme.onSurface
    )
    Surface(shape = MaterialTheme.shapes.small, color = bg) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp), modifier = Modifier.heightIn(max = 28.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
}

@Composable
private fun TodoCard(todo: Todo) {
    val scheme = MaterialTheme.colorScheme
    val (cardBg, cardFg) = statusColors(todo.status, scheme)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = todo.title, style = MaterialTheme.typography.titleSmall, color = cardFg, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (!todo.status.isNullOrBlank()) {
                    val (chipBg, chipFg) = statusChipColors(todo.status, scheme)
                    Surface(shape = MaterialTheme.shapes.small, color = chipBg) { Text(text = statusText(todo.status!!), style = MaterialTheme.typography.labelSmall, color = chipFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                }
                if (!todo.priority.isNullOrBlank()) {
                    val (pBg, pFg) = priorityChipColors(todo.priority, scheme)
                    Surface(shape = MaterialTheme.shapes.small, color = pBg) { Text(text = priorityText(todo.priority!!), style = MaterialTheme.typography.labelSmall, color = pFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                }
            }
            Text(text = "点击查看详情", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

private fun statusColors(status: String?, scheme: ColorScheme): Pair<Color, Color> {
    return when (status?.lowercase()) {
        "in_progress" -> scheme.primaryContainer to scheme.onPrimaryContainer
        "pending", "waiting" -> scheme.secondaryContainer to scheme.onSecondaryContainer
        "completed" -> scheme.surfaceVariant to scheme.onSurface
        else -> scheme.surfaceVariant to scheme.onSurface
    }
}

private fun statusChipColors(status: String?, scheme: ColorScheme): Pair<Color, Color> {
    return when (status?.lowercase()) {
        "in_progress" -> scheme.primary to scheme.onPrimary
        "pending", "waiting" -> scheme.secondary to scheme.onSecondary
        "completed" -> scheme.tertiary to scheme.onTertiary
        else -> scheme.surfaceVariant to scheme.onSurface
    }
}

private fun stateActionChipColors(status: String, selected: Boolean, scheme: ColorScheme): Pair<Color, Color> {
    if (!selected) return scheme.surfaceVariant to scheme.onSurface
    return when (status.lowercase()) {
        "in_progress" -> scheme.primaryContainer to scheme.onPrimaryContainer
        "pending", "waiting" -> scheme.secondaryContainer to scheme.onSecondaryContainer
        "completed" -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
}

private fun statusText(status: String): String {
    return when (status.lowercase()) {
        "in_progress" -> "进行中"
        "pending", "waiting" -> "等待"
        "completed" -> "已完成"
        else -> status
    }
}

private fun priorityChipColors(priority: String?, scheme: ColorScheme): Pair<Color, Color> {
    return when (priority?.lowercase()) {
        "urgent" -> scheme.errorContainer to scheme.onErrorContainer
        "high" -> scheme.primaryContainer to scheme.onPrimaryContainer
        "medium" -> scheme.secondaryContainer to scheme.onSecondaryContainer
        "low" -> scheme.surfaceVariant to scheme.onSurface
        else -> scheme.surfaceVariant to scheme.onSurface
    }
}

private fun priorityText(priority: String): String {
    return when (priority.lowercase()) {
        "urgent" -> "紧急"
        "high" -> "高优先级"
        "medium" -> "中优先级"
        "low" -> "低优先级"
        else -> priority
    }
}

private fun parseTags(tags: String?): List<String> {
    if (tags.isNullOrBlank()) return emptyList()
    val t = tags.trim()
    return runCatching {
        if (t.startsWith("[") && t.endsWith("]")) {
            t.removePrefix("[").removeSuffix("]").split(',').map { it.trim().trim('"') }.filter { it.isNotBlank() }
        } else emptyList()
    }.getOrElse { emptyList() }
}

private fun parseMentionsJson(mentions: String?): List<String> {
    if (mentions.isNullOrBlank()) return emptyList()
    val t = mentions.trim()
    return runCatching {
        if (t.startsWith("[") && t.endsWith("]")) {
            t.removePrefix("[").removeSuffix("]").split(',').map { it.trim().trim('"') }.filter { it.isNotBlank() }
        } else emptyList()
    }.getOrElse { emptyList() }
}

private fun parseMentionsFromText(text: String?): List<String> {
    if (text.isNullOrBlank()) return emptyList()
    val regex = Regex("@([A-Za-z0-9_]+)")
    return regex.findAll(text).map { it.groupValues[1] }.distinct().toList()
}
private fun encodeMentionsJson(list: List<String>): String {
    if (list.isEmpty()) return "[]"
    return list.joinToString(prefix = "[", postfix = "]") { "\"" + it + "\"" }
}

private fun buildSuggestionBase(t: Todo, comments: List<com.blog.myandroidblog.data.models.TodoComment>): List<String> {
    val set = linkedSetOf<String>()
    if (!t.author.isNullOrBlank()) set.add(t.author!!)
    if (!t.assignee.isNullOrBlank()) set.add(t.assignee!!)
    comments.forEach { c ->
        c.author?.let { set.add(it) }
        parseMentionsJson(c.mentions).forEach { set.add(it) }
    }
    return set.toList()
}

private fun replaceLastMention(text: String, replacement: String): String {
    val at = text.lastIndexOf('@')
    if (at < 0) return text
    val tail = text.substring(at + 1).takeWhile { it.isLetterOrDigit() || it == '_' }
    val end = at + 1 + tail.length
    return text.substring(0, at + 1) + replacement + text.substring(end)
}
@Composable
private fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onSubmit: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent
    ) {
        Row(
            modifier = modifier.height(32.dp).padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
                if (value.isBlank()) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            if (onSubmit != null) {
                TextButton(
                    onClick = onSubmit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) { Text("提交", style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
private fun CommentsTree(
    comments: List<com.blog.myandroidblog.data.models.TodoComment>,
    level: Int = 0,
    onReply: (com.blog.myandroidblog.data.models.TodoComment) -> Unit = {},
    parentAuthor: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        comments.forEach { c ->
            Column(modifier = Modifier.padding(start = (level * 12).dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    val replyTo = parentAuthor?.let { " 回复 $it" } ?: ""
                    Text(
                        text = "${c.author ?: "匿名"}$replyTo：${c.content}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onReply(c) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) { Text("回复", style = MaterialTheme.typography.labelSmall) }
                }
                if (!c.created_at.isNullOrBlank()) {
                    Text(
                        text = c.created_at,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                c.replies?.let { replies ->
                    if (!replies.isNullOrEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        if (!expanded) {
                            Row(horizontalArrangement = Arrangement.Start) {
                                TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) { Text("展开${replies.size}条回复", style = MaterialTheme.typography.labelSmall) }
                            }
                        } else {
                            CommentsTree(replies, level + 1, onReply, parentAuthor = c.author)
                            Row(horizontalArrangement = Arrangement.Start) {
                                TextButton(onClick = { expanded = false }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) { Text("收起", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}
