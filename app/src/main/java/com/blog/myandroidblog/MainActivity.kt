package com.blog.myandroidblog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.blog.myandroidblog.data.settings.SettingsManager
import com.blog.myandroidblog.ui.theme.MyAndroidBlogTheme
import com.blog.myandroidblog.ui.AppRoot

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)
        enableEdgeToEdge()
        setContent {
            MyAndroidBlogTheme(settingsManager = settingsManager) {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    AppRoot(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}