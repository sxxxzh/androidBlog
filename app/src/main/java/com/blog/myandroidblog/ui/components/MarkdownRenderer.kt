package com.blog.myandroidblog.ui.components

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import androidx.compose.material3.MaterialTheme

@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    isDarkMode: Boolean = false,
    onLinkClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    val markwon = remember(isDarkMode) {
        createMarkwon(context, isDarkMode, onLinkClick)
    }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                // Configure text view
                setPadding(16, 16, 16, 16)
                setLineSpacing(8f, 1.2f)
            }
        },
        update = { textView ->
            val spanned = markwon.toMarkdown(markdown)
            textView.text = spanned
        }
    )
}

private fun createMarkwon(
    context: Context,
    isDarkMode: Boolean,
    onLinkClick: (String) -> Unit
): Markwon {
    return Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                val textColor = if (isDarkMode) Color.White.toArgb() else Color.Black.toArgb()
                val linkColor = if (isDarkMode) 0xFF64B5F6.toInt() else 0xFF1976D2.toInt()
                
                builder
                    .linkColor(linkColor)
                    .codeBackgroundColor(if (isDarkMode) 0xFF2D2D2D.toInt() else 0xFFF5F5F5.toInt())
                    .codeTextColor(textColor)
            }
            
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.linkResolver { view, link ->
                    onLinkClick(link)
                }
            }
        })
        .build()
}