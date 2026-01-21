// ============================================================================
// build.gradle.kts (Module: app)
// Конфигурация модуля приложения
// ============================================================================
//
// Этот файл описывает настройки сборки Android-модуля.
// Включает зависимости, версии SDK, и другие параметры.
//
// ============================================================================

plugins {
    // Android Application плагин
    id("com.android.application")
    
    // Kotlin для Android
    id("org.jetbrains.kotlin.android")
}

android {
    // ========================================================================
    // ОСНОВНЫЕ НАСТРОЙКИ
    // ========================================================================
    
    // Пространство имён для R-класса
    namespace = "com.playgama.games"
    
    // Версия Compile SDK (для компиляции)
    compileSdk = 34
    
    defaultConfig {
        // Уникальный идентификатор приложения
        applicationId = "com.playgama.games"
        
        // Минимальная версия Android (API 24 = Android 7.0)
        // Поддерживает ~94% устройств
        minSdk = 24
        
        // Целевая версия SDK
        targetSdk = 34
        
        // Версия приложения
        versionCode = 1
        versionName = "1.0.0"
        
        // Конфигурация тестов
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    // ========================================================================
    // ТИПЫ СБОРКИ
    // ========================================================================
    
    buildTypes {
        // Release-сборка
        release {
            // Включаем минификацию кода
            isMinifyEnabled = true
            
            // ProGuard правила
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        // Debug-сборка (по умолчанию)
        debug {
            isMinifyEnabled = false
            
            // Суффикс для debug-версии
            applicationIdSuffix = ".debug"
        }
    }
    
    // ========================================================================
    // НАСТРОЙКИ KOTLIN
    // ========================================================================
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // ========================================================================
    // VIEW BINDING (опционально)
    // ========================================================================
    
    buildFeatures {
        viewBinding = true
    }
}

// ============================================================================
// ЗАВИСИМОСТИ
// ============================================================================

dependencies {
    // ========================================================================
    // ANDROID CORE
    // ========================================================================
    
    // Kotlin Core Extensions
    implementation("androidx.core:core-ktx:1.12.0")
    
    // AppCompat для обратной совместимости
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Material Design компоненты
    implementation("com.google.android.material:material:1.11.0")
    
    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // ========================================================================
    // LIFECYCLE & COROUTINES
    // ========================================================================
    
    // Lifecycle-aware components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Kotlin Coroutines для асинхронных операций
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ========================================================================
    // RECYCLERVIEW
    // ========================================================================
    
    // RecyclerView для списка игр
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ========================================================================
    // JSON PARSING
    // ========================================================================
    
    // Gson для парсинга JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // ========================================================================
    // IMAGE LOADING
    // ========================================================================
    
    // Glide для загрузки изображений
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // ========================================================================
    // WEBKIT (для WebView)
    // ========================================================================
    
    // AndroidX WebKit для дополнительных возможностей WebView
    implementation("androidx.webkit:webkit:1.10.0")
    
    // ========================================================================
    // ТЕСТИРОВАНИЕ
    // ========================================================================
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
