package com.blog.myandroidblog.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

// Extension for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsManager(private val context: Context) {
    
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val FONT_SIZE = floatPreferencesKey("font_size")
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val AUTO_DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("auto_download_wifi_only")
        private val ENABLE_SYNTAX_HIGHLIGHTING = booleanPreferencesKey("enable_syntax_highlighting")
        private val MARKDOWN_RENDERING_ENGINE = stringPreferencesKey("markdown_rendering_engine")
        private val ENABLE_OFFLINE_MODE = booleanPreferencesKey("enable_offline_mode")
        
        // Default values
        const val DEFAULT_FONT_SIZE = 16f
        const val DEFAULT_DARK_MODE = false
        const val DEFAULT_NOTIFICATIONS = true
        const val DEFAULT_WIFI_ONLY_DOWNLOADS = true
        const val DEFAULT_SYNTAX_HIGHLIGHTING = true
        const val DEFAULT_MARKDOWN_ENGINE = "markwon"
        const val DEFAULT_OFFLINE_MODE = false
    }
    
    // Dark mode
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: DEFAULT_DARK_MODE
        }
    
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
    
    // Font size
    val fontSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SIZE] ?: DEFAULT_FONT_SIZE
        }
    
    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size.coerceIn(12f, 24f)
        }
    }
    
    // Notifications
    val enableNotifications: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_NOTIFICATIONS] ?: DEFAULT_NOTIFICATIONS
        }
    
    suspend fun setEnableNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = enabled
        }
    }
    
    // WiFi-only downloads
    val autoDownloadWifiOnly: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_DOWNLOAD_WIFI_ONLY] ?: DEFAULT_WIFI_ONLY_DOWNLOADS
        }
    
    suspend fun setAutoDownloadWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_WIFI_ONLY] = wifiOnly
        }
    }
    
    // Syntax highlighting
    val enableSyntaxHighlighting: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_SYNTAX_HIGHLIGHTING] ?: DEFAULT_SYNTAX_HIGHLIGHTING
        }
    
    suspend fun setEnableSyntaxHighlighting(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_SYNTAX_HIGHLIGHTING] = enabled
        }
    }
    
    // Markdown rendering engine
    val markdownRenderingEngine: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MARKDOWN_RENDERING_ENGINE] ?: DEFAULT_MARKDOWN_ENGINE
        }
    
    suspend fun setMarkdownRenderingEngine(engine: String) {
        context.dataStore.edit { preferences ->
            preferences[MARKDOWN_RENDERING_ENGINE] = engine
        }
    }
    
    // Offline mode
    val enableOfflineMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_OFFLINE_MODE] ?: DEFAULT_OFFLINE_MODE
        }
    
    suspend fun setEnableOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_OFFLINE_MODE] = enabled
        }
    }
    
    // Get all settings at once
    suspend fun getAllSettings(): AppSettings {
        val preferences = context.dataStore.data.first()
        return AppSettings(
            isDarkMode = preferences[IS_DARK_MODE] ?: DEFAULT_DARK_MODE,
            fontSize = preferences[FONT_SIZE] ?: DEFAULT_FONT_SIZE,
            enableNotifications = preferences[ENABLE_NOTIFICATIONS] ?: DEFAULT_NOTIFICATIONS,
            autoDownloadWifiOnly = preferences[AUTO_DOWNLOAD_WIFI_ONLY] ?: DEFAULT_WIFI_ONLY_DOWNLOADS,
            enableSyntaxHighlighting = preferences[ENABLE_SYNTAX_HIGHLIGHTING] ?: DEFAULT_SYNTAX_HIGHLIGHTING,
            markdownRenderingEngine = preferences[MARKDOWN_RENDERING_ENGINE] ?: DEFAULT_MARKDOWN_ENGINE,
            enableOfflineMode = preferences[ENABLE_OFFLINE_MODE] ?: DEFAULT_OFFLINE_MODE
        )
    }
    
    // Reset all settings to defaults
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = DEFAULT_DARK_MODE
            preferences[FONT_SIZE] = DEFAULT_FONT_SIZE
            preferences[ENABLE_NOTIFICATIONS] = DEFAULT_NOTIFICATIONS
            preferences[AUTO_DOWNLOAD_WIFI_ONLY] = DEFAULT_WIFI_ONLY_DOWNLOADS
            preferences[ENABLE_SYNTAX_HIGHLIGHTING] = DEFAULT_SYNTAX_HIGHLIGHTING
            preferences[MARKDOWN_RENDERING_ENGINE] = DEFAULT_MARKDOWN_ENGINE
            preferences[ENABLE_OFFLINE_MODE] = DEFAULT_OFFLINE_MODE
        }
    }
}

data class AppSettings(
    val isDarkMode: Boolean,
    val fontSize: Float,
    val enableNotifications: Boolean,
    val autoDownloadWifiOnly: Boolean,
    val enableSyntaxHighlighting: Boolean,
    val markdownRenderingEngine: String,
    val enableOfflineMode: Boolean
)

// Theme helper
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

// Font size presets
enum class FontSizePreset(val size: Float, val displayName: String) {
    SMALL(12f, "Small"),
    NORMAL(16f, "Normal"),
    LARGE(18f, "Large"),
    EXTRA_LARGE(20f, "Extra Large"),
    HUGE(24f, "Huge")
}