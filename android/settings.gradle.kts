// ============================================================================
// settings.gradle.kts
// Настройки проекта Gradle
// ============================================================================

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Название проекта
rootProject.name = "PlaygamaGames"

// Включаем модуль app
include(":app")
