plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

import java.util.Properties

android {
    namespace = "com.blog.myandroidblog"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.blog.myandroidblog"
        minSdk = 33
        targetSdk = 36
        versionCode = 10104
        versionName = "1.1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

// Custom signing config via keystore.properties (optional)
val keystorePropsFile = rootProject.file("keystore.properties")
if (keystorePropsFile.exists()) {
    val keystoreProperties = Properties().apply {
        keystorePropsFile.inputStream().use { load(it) }
    }
    val storeFilePath = keystoreProperties.getProperty("storeFile")
    val hasKeystore = !storeFilePath.isNullOrBlank() && file(storeFilePath).exists()
    if (hasKeystore) {
        android {
            signingConfigs {
                create("release") {
                    storeFile = file(storeFilePath!!)
                    storePassword = keystoreProperties.getProperty("storePassword")
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                }
            }
            buildTypes {
                getByName("release") {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
        }
    }
}

// Output APK name customization can be added via androidComponents or variant outputs.
// Skipping here to keep build stable; we can provide a dedicated Gradle task to rename after build.

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    // Markdown support
    implementation(libs.markwon.core)
    implementation(libs.markwon.html)
    implementation(libs.markwon.image)
    implementation(libs.markwon.syntax.highlight)
    implementation(libs.markwon.ext.tables)
    implementation(libs.markwon.linkify)
    
    // Download manager and background tasks
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.recyclerview)
    
    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // File handling
    implementation(libs.commons.io)
    
    // Splash screen
    implementation(libs.androidx.core.splashscreen)
    
    // Fix annotation conflicts
    configurations.all {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
