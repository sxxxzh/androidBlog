package com.blog.myandroidblog.ui.screens

import android.webkit.JavascriptInterface
import android.content.Context
import android.util.Log

/**
 * JavaScript interface for WebView to handle keyboard events and other interactions
 */
class WebViewJavaScriptInterface(
    private val context: Context,
    private val onKeyboardStateChange: (Boolean) -> Unit,
    private val onOpenCamera: () -> Unit,
    private val onOpenAlbum: () -> Unit
) {
    
    @JavascriptInterface
    fun onInputFocused(tagName: String) {
        Log.d("WebViewJS", "Input focused: $tagName")
        // Notify that keyboard is likely to appear
        onKeyboardStateChange(true)
    }
    
    @JavascriptInterface
    fun onInputBlurred(tagName: String) {
        Log.d("WebViewJS", "Input blurred: $tagName")
        // Notify that keyboard is likely to disappear
        onKeyboardStateChange(false)
    }
    
    @JavascriptInterface
    fun getKeyboardHeight(): Int {
        // This can be enhanced to return actual keyboard height
        return 0
    }
    
    @JavascriptInterface
    fun scrollToBottom() {
        // Can be used by web content to scroll to bottom when keyboard appears
        Log.d("WebViewJS", "Scroll to bottom requested")
    }
    
    @JavascriptInterface
    fun adjustContentForKeyboard(keyboardHeight: Int) {
        Log.d("WebViewJS", "Adjust content for keyboard: $keyboardHeight")
        // Can be called from JavaScript to request content adjustment
    }

    @JavascriptInterface
    fun openCamera() {
        onOpenCamera()
    }

    @JavascriptInterface
    fun openAlbum() {
        onOpenAlbum()
    }
}