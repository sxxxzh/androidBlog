package com.blog.myandroidblog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.regex.Pattern

// Content block types for structured markdown rendering
sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Image(val url: String, val altText: String) : ContentBlock()
    data class Header(val text: String, val level: Int) : ContentBlock()
    data class Code(val content: String) : ContentBlock()
    data class Quote(val content: String) : ContentBlock()
    data class ListItem(val content: String, val isOrdered: Boolean, val level: Int) : ContentBlock()
    data class CodeBlock(val content: String, val language: String?) : ContentBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : ContentBlock()
}

@Composable
fun EnhancedMarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isDarkMode: Boolean = false,
    onLinkClick: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val textColor = if (isDarkMode) colorScheme.onSurface else colorScheme.onSurface
    val backgroundColor = if (isDarkMode) colorScheme.surface else colorScheme.surface
    
    // Dynamic color scheme for better readability
    val primaryColor = colorScheme.primary
    val secondaryColor = colorScheme.secondary
    val errorColor = colorScheme.error
    val context = LocalContext.current
    
    // Handle empty or null content
    if (markdown.isBlank()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No content available",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    
    // Parse markdown into structured content blocks
    val contentBlocks = remember(markdown) {
        parseMarkdownToBlocks(markdown)
    }
    
    // Render content blocks
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        contentBlocks.forEach { block ->
            when (block) {
                is ContentBlock.Text -> {
                    TextBlock(block, isDarkMode)
                }
                is ContentBlock.Image -> {
                    ImageBlock(block, isDarkMode)
                }
                is ContentBlock.Header -> {
                    HeaderBlock(block, isDarkMode)
                }
                is ContentBlock.Code -> {
                    CodeBlock(block, isDarkMode)
                }
                is ContentBlock.Quote -> {
                    QuoteBlock(block, isDarkMode)
                }
                is ContentBlock.ListItem -> {
                    ListItemBlock(block, isDarkMode)
                }
                is ContentBlock.CodeBlock -> {
                    CodeBlockEnhanced(block, isDarkMode)
                }
                is ContentBlock.Table -> {
                    TableBlock(block, isDarkMode)
                }
            }
        }
    }
}

@Composable
private fun ListItemBlock(block: ContentBlock.ListItem, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    }
    
    val bulletColor = MaterialTheme.colorScheme.primary.copy(
        alpha = if (isDarkMode) 0.8f else 0.9f
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indentation based on level
        repeat(block.level) {
            Spacer(modifier = Modifier.width(20.dp))
        }
        
        // Bullet or number
        Box(
            modifier = Modifier.size(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = bulletColor
            ) {}
        }
        
        // List item content
        Text(
            text = buildAnnotatedString {
                val formatted = parseInlineFormatting(block.content)
                parseFormattedText(formatted, isDarkMode)
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 22.sp
            ),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CodeBlockEnhanced(block: ContentBlock.CodeBlock, isDarkMode: Boolean) {
    val context = LocalContext.current
    var showCopyToast by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp),
        color = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF5F5F5),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Language label if available (no copy button)
            if (block.language != null) {
                Text(
                    text = block.language,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkMode) Color(0xFF569CD6) else Color(0xFF007ACC),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            // Code content (clickable to copy, no visible copy button)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Code", block.content)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
                    },
                color = Color.Transparent
            ) {
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    color = if (isDarkMode) Color.White else Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
    
    // Copy success toast (only show briefly)
    // no persistent toast UI needed
}

@Composable
private fun ImageGallery(
    imageUrls: List<String>,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        imageUrls.forEach { imageUrl ->
            ImageCard(
                imageUrl = imageUrl,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun ImageCard(
    imageUrl: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp), // Add height constraint to card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp) // Add some padding
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .build(),
                contentDescription = "Markdown image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onError = { error ->
                    // Log error for debugging but don't crash
                    android.util.Log.e("EnhancedMarkdownRenderer", "Failed to load image: $imageUrl", error.result.throwable)
                }
            )
        }
    }
}

private fun parseMarkdownToBlocks(markdown: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = markdown.split("\n")
    
    var currentTextBlock = StringBuilder()
    var inCodeBlock = false
    var codeBlockContent = StringBuilder()
    var codeBlockLanguage: String? = null
    var inTable = false
    var tableHeaders: List<String> = emptyList()
    var tableRows: MutableList<List<String>> = mutableListOf()
    
    fun flushTextBlock() {
        if (currentTextBlock.isNotEmpty()) {
            // Parse inline formatting within text blocks
            val parsedContent = parseInlineFormatting(currentTextBlock.toString().trim())
            if (parsedContent.isNotEmpty()) {
                blocks.add(ContentBlock.Text(parsedContent))
            }
            currentTextBlock = StringBuilder()
        }
    }
    
    fun flushCodeBlock() {
        if (codeBlockContent.isNotEmpty()) {
            blocks.add(ContentBlock.CodeBlock(codeBlockContent.toString().trim(), codeBlockLanguage))
            codeBlockContent = StringBuilder()
            codeBlockLanguage = null
        }
    }
    
    fun flushTable() {
        if (tableHeaders.isNotEmpty() && tableRows.isNotEmpty()) {
            blocks.add(ContentBlock.Table(tableHeaders, tableRows.toList()))
            tableHeaders = emptyList()
            tableRows.clear()
        }
    }
    
    for (line in lines) {
        when {
            // Code block handling (multi-line)
            line.startsWith("```") -> {
                if (inCodeBlock) {
                    // End of code block
                    flushCodeBlock()
                    inCodeBlock = false
                } else {
                    // Start of code block
                    flushTextBlock()
                    flushTable()
                    inTable = false
                    inCodeBlock = true
                    codeBlockLanguage = line.substring(3).trim().takeIf { it.isNotEmpty() }
                }
            }
            inCodeBlock -> {
                codeBlockContent.append(line).append("\n")
            }
            // Table handling
            line.contains("|") -> {
                val tableRow = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                
                if (tableRow.isNotEmpty()) {
                    if (!inTable) {
                        // Start of new table
                        flushTextBlock()
                        inTable = true
                        tableHeaders = tableRow
                        tableRows.clear()
                    } else if (line.matches(Regex("^\\s*\\|?(?:\\s*[-:]+\\s*\\|)+\\s*[-:]+\\s*\\|?\\s*$"))) {
                        // Separator line - skip it
                        continue
                    } else {
                        // Regular table row
                        tableRows.add(tableRow)
                    }
                }
            }
            inTable && !line.contains("|") -> {
                // End of table
                flushTable()
                inTable = false
            }
            // Headers
            line.startsWith("# ") -> {
                flushTextBlock()
                blocks.add(ContentBlock.Header(line.substring(2), 1))
            }
            line.startsWith("## ") -> {
                flushTextBlock()
                blocks.add(ContentBlock.Header(line.substring(3), 2))
            }
            line.startsWith("### ") -> {
                flushTextBlock()
                blocks.add(ContentBlock.Header(line.substring(4), 3))
            }
            line.startsWith("#### ") -> {
                flushTextBlock()
                blocks.add(ContentBlock.Header(line.substring(5), 4))
            }
            // Lists (unordered and ordered)
            line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                flushTextBlock()
                val content = line.trimStart().substring(2)
                val level = (line.length - line.trimStart().length) / 2
                blocks.add(ContentBlock.ListItem(content, false, level))
            }
            Regex("^\\s*\\d+\\. ").containsMatchIn(line) -> {
                flushTextBlock()
                val content = line.trimStart().substringAfter(". ")
                val level = (line.length - line.trimStart().length) / 2
                blocks.add(ContentBlock.ListItem(content, true, level))
            }
            // Horizontal rule
            line.trim() == "---" || line.trim() == "***" || line.trim() == "___" -> {
                flushTextBlock()
                // Could add a special horizontal rule block if needed
            }
            // Images
            line.contains("![") && line.contains("](") -> {
                flushTextBlock()
                val imagePattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)")
                val matcher = imagePattern.matcher(line)
                
                if (matcher.find()) {
                    val altText = matcher.group(1) ?: "Image"
                    val imageUrl = matcher.group(2)?.trim() ?: ""
                    
                    if (imageUrl.isNotEmpty() && (imageUrl.startsWith("http") || imageUrl.startsWith("https"))) {
                        blocks.add(ContentBlock.Image(imageUrl, altText))
                    } else {
                        // If not a valid URL, treat as text
                        currentTextBlock.append(line).append("\n")
                    }
                } else {
                    currentTextBlock.append(line).append("\n")
                }
            }
            // Quotes
            line.startsWith("> ") -> {
                flushTextBlock()
                blocks.add(ContentBlock.Quote(line.substring(2)))
            }
            // Regular text
            line.isNotBlank() -> {
                currentTextBlock.append(line).append("\n")
            }
            // Empty lines - flush current text block
            else -> {
                flushTextBlock()
            }
        }
    }
    
    // Handle any remaining content
    if (inCodeBlock) {
        flushCodeBlock()
    } else if (inTable) {
        flushTable()
    } else {
        flushTextBlock()
    }
    
    return blocks
}

private fun AnnotatedString.Builder.parseFormattedText(content: String, isDarkMode: Boolean) {
    // Process the content sequentially to handle formatting markers
    var remainingText = content
    
    while (remainingText.isNotEmpty()) {
        // Look for the next formatting marker
        val boldMatch = Regex("【BOLD:([^】]+)】").find(remainingText)
        val italicMatch = Regex("【ITALIC:([^】]+)】").find(remainingText)
        val codeMatch = Regex("【CODE:([^】]+)】").find(remainingText)
        val strikeMatch = Regex("【STRIKE:([^】]+)】").find(remainingText)
        val linkMatch = Regex("【LINK:([^|】]+)\\|([^】]+)】").find(remainingText)
        
        // Find the earliest match
        val matches = listOfNotNull(boldMatch, italicMatch, codeMatch, strikeMatch, linkMatch)
        val earliestMatch = matches.minByOrNull { it.range.first }
        
        if (earliestMatch != null) {
            // Add text before the match
            val textBefore = remainingText.substring(0, earliestMatch.range.first)
            if (textBefore.isNotEmpty()) {
                // Check if textBefore contains nested formatting
                if (textBefore.contains("【")) {
                    // Recursively process nested formatting
                    parseFormattedText(textBefore, isDarkMode)
                } else {
                    // Regular text
                    withStyle(
                        style = SpanStyle(
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(textBefore)
                    }
                }
            }
            
            // Process the formatted text (check for nested formatting)
            val formatType = when {
                boldMatch == earliestMatch -> "BOLD"
                italicMatch == earliestMatch -> "ITALIC"
                codeMatch == earliestMatch -> "CODE"
                linkMatch == earliestMatch -> "LINK"
                strikeMatch == earliestMatch -> "STRIKE"
                else -> ""
            }
            
            val formattedText = earliestMatch.groupValues[1]
            val formattedUrl = if (linkMatch == earliestMatch) earliestMatch.groupValues.getOrNull(2) else null
            
            // Check if formatted text contains nested markers
            if (formattedText.contains("【")) {
                // Handle nested formatting based on type
                when (formatType) {
                    "BOLD" -> {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        ) {
                            parseFormattedText(formattedText, isDarkMode)
                        }
                    }
                    "ITALIC" -> {
                        withStyle(
                            style = SpanStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        ) {
                            parseFormattedText(formattedText, isDarkMode)
                        }
                    }
                    "CODE" -> {
                        withStyle(
                            style = SpanStyle(
                                background = if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                                color = if (isDarkMode) Color(0xFF569CD6) else Color(0xFFD73A49),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        ) {
                            parseFormattedText(formattedText, isDarkMode)
                        }
                    }
                    "STRIKE" -> {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.LineThrough,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            parseFormattedText(formattedText, isDarkMode)
                        }
                    }
                    "LINK" -> {
                        pushStringAnnotation(tag = "URL", annotation = formattedUrl ?: formattedText)
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1976D2),
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            parseFormattedText(formattedText, isDarkMode)
                        }
                        pop()
                    }
                }
            } else {
                // Simple formatted text without nesting
                when (formatType) {
                    "BOLD" -> {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        ) {
                            append(formattedText)
                        }
                    }
                    "ITALIC" -> {
                        withStyle(
                            style = SpanStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        ) {
                            append(formattedText)
                        }
                    }
                    "CODE" -> {
                        withStyle(
                            style = SpanStyle(
                                background = if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                                color = if (isDarkMode) Color(0xFF569CD6) else Color(0xFFD73A49),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        ) {
                            append(formattedText)
                        }
                    }
                    "STRIKE" -> {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.LineThrough,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            append(formattedText)
                        }
                    }
                    "LINK" -> {
                        val url = formattedUrl ?: formattedText
                        pushStringAnnotation(tag = "URL", annotation = url)
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1976D2),
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(formattedText)
                        }
                        pop()
                    }
                }
            }
            
            // Update remaining text
            remainingText = remainingText.substring(earliestMatch.range.last + 1)
        } else {
            // No more matches, add remaining text
            if (remainingText.isNotEmpty()) {
                if (remainingText.contains("【")) {
                    // Process any remaining nested formatting
                    parseFormattedText(remainingText, isDarkMode)
                } else {
                    // Regular text
                    withStyle(
                        style = SpanStyle(
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(remainingText)
                    }
                }
            }
            break
        }
    }
}

private fun parseInlineFormatting(text: String): String {
    // Process inline formatting markers with proper nesting support
    var processedText = text
    processedText = processedText.replace("&lt;", "<").replace("&gt;", ">")
    
    // Handle `inline code` first (highest priority, shouldn't contain other formatting)
    processedText = processedText.replace(Regex("`([^`]+)`")) { match ->
        "【CODE:${match.groupValues[1]}】"
    }
    
    // Handle **bold** text - allow inner `*` and other markers, match minimally
    processedText = processedText.replace(Regex("\\*\\*(.+?)\\*\\*")) { match ->
        "【BOLD:${match.groupValues[1]}】"
    }
    
    // Handle *italic* text - only match single asterisks not part of bold
    // Use negative lookbehind and lookahead to avoid matching **bold** markers
    processedText = processedText.replace(Regex("(?<!\\*)\\*(.+?)\\*(?!\\*)")) { match ->
        "【ITALIC:${match.groupValues[1]}】"
    }
    
    // Handle ~~strikethrough~~
    processedText = processedText.replace(Regex("~~(.+?)~~")) { match ->
        "【STRIKE:${match.groupValues[1]}】"
    }

    // Fallback: bare URLs without brackets (ensure minimal capture)
    processedText = processedText.replace(Regex("(?<![A-Za-z0-9_])(https?://[A-Za-z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]+)")) { match ->
        val url = match.groupValues[1]
        "【LINK:${url}|${url}】"
    }
    
    return processedText
}

@Composable
private fun TextBlock(block: ContentBlock.Text, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    }
    
    val context = LocalContext.current
    val annotated = remember(block.content, isDarkMode) {
        buildAnnotatedString { parseFormattedText(block.content, isDarkMode) }
    }
    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.5.sp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(vertical = 6.dp),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { ann ->
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Link", ann.item)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
private fun ImageBlock(block: ContentBlock.Image, isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(block.url)
                    .crossfade(true)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .build(),
                contentDescription = block.altText,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                contentScale = ContentScale.Fit,
                onError = { error ->
                    android.util.Log.e("EnhancedMarkdownRenderer", "Failed to load image: ${block.url}", error.result.throwable)
                }
            )
            
            if (block.altText.isNotBlank()) {
                Text(
                    text = block.altText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeaderBlock(block: ContentBlock.Header, isDarkMode: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val headerColors = when (block.level) {
        1 -> if (isDarkMode) colorScheme.primary.copy(alpha = 0.9f) else colorScheme.primary
        2 -> if (isDarkMode) colorScheme.secondary.copy(alpha = 0.9f) else colorScheme.secondary
        else -> if (isDarkMode) colorScheme.onSurface.copy(alpha = 0.8f) else colorScheme.onSurface.copy(alpha = 0.7f)
    }
    
    val textStyle = when (block.level) {
        1 -> MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        2 -> MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp
        )
        3 -> MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.2).sp
        )
        else -> MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Medium
        )
    }
    val context = LocalContext.current
    val annotated = remember(block.text) {
        val formatted = parseInlineFormatting(block.text)
        buildAnnotatedString { parseFormattedText(formatted, isDarkMode) }
    }
    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = textStyle.copy(color = headerColors),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(
                vertical = when (block.level) {
                    1 -> 12.dp
                    2 -> 10.dp
                    else -> 8.dp
                }
            ),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { ann ->
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Link", ann.item)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
private fun CodeBlock(block: ContentBlock.Code, isDarkMode: Boolean) {
    val context = LocalContext.current
    var showCopyToast by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp)
            .clickable { 
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Code", block.content)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
            },
        color = if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = block.content,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = if (isDarkMode) Color.White else Color.Black,
            modifier = Modifier.padding(12.dp)
        )
    }
    
    // Copy success toast (only show briefly)
    if (showCopyToast) {
        androidx.compose.runtime.LaunchedEffect(showCopyToast) {
            kotlinx.coroutines.delay(1500)
            showCopyToast = false
        }
    }
}

@Composable
private fun TableBlock(block: ContentBlock.Table, isDarkMode: Boolean) {
    val headerBackground = if (isDarkMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    val rowAltBackground = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.06f)
    val textColor = if (isDarkMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (block.headers.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().background(headerBackground)) {
                    block.headers.forEach { header ->
                        Box(modifier = Modifier.weight(1f).padding(12.dp), contentAlignment = Alignment.CenterStart) {
                            val ann = remember(header, isDarkMode) {
                                val formatted = parseInlineFormatting(header)
                                buildAnnotatedString { parseFormattedText(formatted, isDarkMode) }
                            }
                            androidx.compose.foundation.text.ClickableText(
                                text = ann,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = textColor),
                                onClick = { offset ->
                                    ann.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { a ->
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Link", a.item)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            block.rows.forEachIndexed { index, row ->
                val rowBg = if (index % 2 == 0) rowAltBackground else Color.Transparent
                Row(modifier = Modifier.fillMaxWidth().background(rowBg).padding(vertical = 1.dp)) {
                    row.forEach { cell ->
                        Box(modifier = Modifier.weight(1f).padding(12.dp), contentAlignment = Alignment.CenterStart) {
                            val ann = remember(cell, isDarkMode) {
                                val formatted = parseInlineFormatting(cell)
                                buildAnnotatedString { parseFormattedText(formatted, isDarkMode) }
                            }
                            androidx.compose.foundation.text.ClickableText(
                                text = ann,
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor.copy(alpha = 0.85f)),
                                onClick = { offset ->
                                    ann.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { a ->
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Link", a.item)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "复制成功", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
                if (index != block.rows.lastIndex) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun QuoteBlock(block: ContentBlock.Quote, isDarkMode: Boolean) {
    val quoteBackground = if (isDarkMode) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
    }
    
    val quoteTextColor = if (isDarkMode) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 6.dp),
        color = quoteBackground,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkMode) 0.7f else 0.9f)
            ) {
                // Empty content - just the colored bar
            }
            
            Text(
                text = buildAnnotatedString {
                    val formatted = parseInlineFormatting(block.content)
                    parseFormattedText(formatted, isDarkMode)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = quoteTextColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun parseMarkdownContent(markdown: String): Pair<String, List<String>> {
    val imagePattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)")
    val matcher = imagePattern.matcher(markdown)
    
    val imageUrls = mutableListOf<String>()
    val textContent = StringBuilder()
    
    var lastEnd = 0
    while (matcher.find()) {
        // Add text before image
        if (matcher.start() > lastEnd) {
            textContent.append(markdown.substring(lastEnd, matcher.start()))
        }
        
        val altText = matcher.group(1)
        val imageUrl = matcher.group(2)?.trim()
        
        if (!imageUrl.isNullOrEmpty() && (imageUrl.startsWith("http") || imageUrl.startsWith("https"))) {
            imageUrls.add(imageUrl)
            // Add placeholder text for image
            textContent.append("\n[Image: ${altText.ifEmpty { "Image" }}]\n")
        }
        
        lastEnd = matcher.end()
    }
    
    // Add remaining text
    if (lastEnd < markdown.length) {
        textContent.append(markdown.substring(lastEnd))
    }
    
    // Also check for HTML img tags
    val htmlPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>")
    val htmlMatcher = htmlPattern.matcher(textContent.toString())
    
    while (htmlMatcher.find()) {
        val url = htmlMatcher.group(1)?.trim()
        if (!url.isNullOrEmpty() && (url.startsWith("http") || url.startsWith("https"))) {
            imageUrls.add(url)
        }
    }
    
    return Pair(textContent.toString(), imageUrls.distinct())
}

private fun parseMarkdownToAnnotatedString(content: String, isDarkMode: Boolean): AnnotatedString {
    return buildAnnotatedString {
        val lines = content.split("\n")
        
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("# ") -> {
                    // Header 1
                    withStyle(
                        style = SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line.substring(2))
                    }
                }
                line.startsWith("## ") -> {
                    // Header 2
                    withStyle(
                        style = SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line.substring(3))
                    }
                }
                line.startsWith("### ") -> {
                    // Header 3
                    withStyle(
                        style = SpanStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line.substring(4))
                    }
                }
                line.startsWith("**") && line.endsWith("**") -> {
                    // Bold text
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line.substring(2, line.length - 2))
                    }
                }
                line.startsWith("*") && line.endsWith("*") -> {
                    // Italic text
                    withStyle(
                        style = SpanStyle(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line.substring(1, line.length - 1))
                    }
                }
                line.contains("[") && line.contains("](") && !line.contains("!") -> {
                    // Link
                    val linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")
                    val matcher = linkPattern.matcher(line)
                    var lastEnd = 0
                    
                    while (matcher.find()) {
                        // Append text before link
                        if (matcher.start() > lastEnd) {
                            append(line.substring(lastEnd, matcher.start()))
                        }
                        
                        val linkText = matcher.group(1)
                        val linkUrl = matcher.group(2)
                        
                        // Append link
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1976D2),
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(linkText ?: "Link")
                        }
                        
                        lastEnd = matcher.end()
                    }
                    
                    // Append remaining text
                    if (lastEnd < line.length) {
                        append(line.substring(lastEnd))
                    }
                }
                line.startsWith("```") -> {
                    // Code block - skip in this function as it's handled by CodeBlockEnhanced
                    withStyle(
                        style = SpanStyle(
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line)
                    }
                }
                line.contains("~~") && line.indexOf("~~") != line.lastIndexOf("~~") -> {
                    // Strikethrough
                    val parts = line.split("~~")
                    for (i in parts.indices) {
                        if (i % 2 == 1) {
                            // Strikethrough text
                            withStyle(
                                style = SpanStyle(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            ) {
                                append(parts[i])
                            }
                        } else {
                            // Regular text
                            withStyle(
                                style = SpanStyle(
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            ) {
                                append(parts[i])
                            }
                        }
                    }
                }
                line.contains("`") && line.indexOf("`") != line.lastIndexOf("`") -> {
                    // Inline code
                    val parts = line.split("`")
                    for (i in parts.indices) {
                        if (i % 2 == 1) {
                            // Inline code
                            withStyle(
                                style = SpanStyle(
                                    background = if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF5F5F5),
                                    color = if (isDarkMode) Color(0xFF569CD6) else Color(0xFFD73A49),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            ) {
                                append(parts[i])
                            }
                        } else {
                            // Regular text
                            withStyle(
                                style = SpanStyle(
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            ) {
                                append(parts[i])
                            }
                        }
                    }
                }
                else -> {
                    // Regular text
                    withStyle(
                        style = SpanStyle(
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    ) {
                        append(line)
                    }
                }
            }
            
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}
