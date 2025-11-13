package com.blog.myandroidblog.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.blog.myandroidblog.ui.theme.MyAndroidBlogTheme

@Preview(showBackground = true)
@Composable
fun EnhancedMarkdownRendererPreview() {
    val sampleMarkdown = """
        # 测试文章标题
        
        这是一个测试文章，包含一些图片。
        
        ## 第一部分
        
        这里是一些普通文本，然后是一张图片：
        
        ![测试图片](https://picsum.photos/400/300)
        
        这是更多文本内容。
        
        ## 第二部分
        
        这里是**粗体文本**和*斜体文本*。
        
        [链接到示例](https://example.com)
        
        代码块示例：
        ```kotlin
        fun main() {
            println("Hello World")
        }
        ```
        
        另一张图片：
        ![风景图片](https://picsum.photos/600/400)
    """.trimIndent()
    
    MyAndroidBlogTheme {
        EnhancedMarkdownRenderer(
            markdown = sampleMarkdown,
            isDarkMode = false
        )
    }
}