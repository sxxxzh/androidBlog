package com.blog.myandroidblog.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity
import android.view.View
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

@Composable
fun ChatHomeWebScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var loaded by remember { mutableStateOf(false) }
    val view = LocalView.current
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { b ->
            val baos = ByteArrayOutputStream()
            b.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val js = """(function(){var d='data:image/jpeg;base64,$base64';var f=document.getElementById('chatFrame');if(f&&f.contentWindow){f.contentWindow.postMessage({type:'android:image',data:{base64:d}},'*');}else{window.dispatchEvent(new CustomEvent('androidImageSelected',{detail:{base64:d}}));}})();""".trimIndent()
            webView?.evaluateJavascript(js, null)
        }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val mime = context.contentResolver.getType(it) ?: "image/*"
                val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val prefix = if (mime.startsWith("image/")) "data:$mime;base64," else "data:image/jpeg;base64,"
                    val dataUrl = prefix + base64
                    val js = """
                        (function(){
                            var d='${dataUrl}';
                            var f=document.getElementById('chatFrame');
                            if(f&&f.contentWindow){
                                f.contentWindow.postMessage({type:'android:image',data:{base64:d}},'*');
                            }else{
                                window.dispatchEvent(new CustomEvent('androidImageSelected',{detail:{base64:d}}));
                            }
                        })();
                    """.trimIndent()
                    webView?.evaluateJavascript(js, null)
                } else {
                    webView?.evaluateJavascript("window.dispatchEvent(new CustomEvent('androidImageError',{detail:{message:'读取失败'}}));", null)
                }
            } catch (_: Exception) {
                webView?.evaluateJavascript("window.dispatchEvent(new CustomEvent('androidImageError',{detail:{message:'处理失败'}}));", null)
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePictureLauncher.launch(null)
        } else {
            webView?.evaluateJavascript("window.dispatchEvent(new CustomEvent('androidImageError',{detail:{message:'未授权相机'}}));", null)
        }
    }
    
    // Handle keyboard visibility and adjust WebView accordingly
    LaunchedEffect(Unit) {
        val activity = context as? Activity ?: return@LaunchedEffect
        
        // Set up keyboard visibility listener
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            
            webView?.let { webview ->
                if (imeVisible && imeHeight > 0) {
                    // Keyboard is visible - adjust WebView layout
                    webview.scrollBy(0, imeHeight)
                    
                    // Inject JavaScript to handle keyboard overlay in the web content
                    val jsCode = """
                        (function() {
                            // Adjust body padding to account for keyboard
                            const keyboardHeight = ${imeHeight};
                            document.body.style.paddingBottom = keyboardHeight + 'px';
                            
                            // Scroll input elements into view
                            const activeElement = document.activeElement;
                            if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {
                                activeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            }
                            
                            // Dispatch custom event for web app to handle
                            window.dispatchEvent(new CustomEvent('androidKeyboardShow', { 
                                detail: { height: keyboardHeight } 
                            }));
                        })();
                    """.trimIndent()
                    webview.evaluateJavascript(jsCode, null)
                } else {
                    // Keyboard is hidden - reset layout
                    val jsCode = """
                        (function() {
                            // Reset body padding
                            document.body.style.paddingBottom = '0px';
                            
                            // Dispatch custom event for web app to handle
                            window.dispatchEvent(new CustomEvent('androidKeyboardHide'));
                        })();
                    """.trimIndent()
                    webview.evaluateJavascript(jsCode, null)
                }
            }
            insets
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Enhanced WebView settings for better keyboard handling
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                    clearCache(true)
                    
                    // Additional settings for better keyboard interaction
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    
                    // Enable hardware acceleration for better performance
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    
                    addJavascriptInterface(
                        WebViewJavaScriptInterface(
                            context,
                            { isKeyboardVisible ->
                                Log.d("ChatHomeWebScreen", "Keyboard state changed: $isKeyboardVisible")
                            },
                            { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            { pickImageLauncher.launch("image/*") }
                        ),
                        "AndroidInterface"
                    )
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            try {
                                com.blog.myandroidblog.data.remote.AuthStore.initialize(context)
                                val token = com.blog.myandroidblog.data.remote.AuthStore.getToken() ?: ""
                                
                                // Enhanced JavaScript injection with keyboard handling
                                val js = """
                                    try {
                                        // Set token
                                        localStorage.setItem('chat_token', '$token');
                                        const tokenInput = document.getElementById('token');
                                        if(tokenInput) { tokenInput.value='$token'; }
                                        
                                        // Add keyboard event listeners for better UX
                                        document.addEventListener('focus', function(e) {
                                            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                                                // Let Android know an input is focused
                                                if (window.AndroidInterface) {
                                                    window.AndroidInterface.onInputFocused(e.target.tagName);
                                                }
                                            }
                                        }, true);
                                        
                                        document.addEventListener('blur', function(e) {
                                            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                                                // Let Android know an input lost focus
                                                if (window.AndroidInterface) {
                                                    window.AndroidInterface.onInputBlurred(e.target.tagName);
                                                }
                                            }
                                        }, true);
                                        
                                        console.log('Android WebView: Chat interface initialized with keyboard handling');
                                    } catch(e) {
                                        console.error('Android WebView initialization error:', e);
                                    }
                                """.trimIndent()
                                view.evaluateJavascript(js, null)
                            } catch (_: Exception) {}
                            super.onPageFinished(view, url)
                        }
                        
                        override fun onReceivedError(
                            view: WebView,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            // Handle errors gracefully
                        }
                    }
                    
                    // Load the chat interface
                    loadUrl("file:///android_asset/www/index.html")
                    webView = this
                    loaded = true
                }
            },
            update = { webview ->
                if (!loaded) {
                    webview.loadUrl("file:///android_asset/www/home.html")
                    loaded = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }
}