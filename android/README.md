# Playgama Games — Android Integration

Example of integrating the Playgama game catalog into an Android app using Kotlin.

---

## ⚠️ Important: Download Your Own Game Catalog!

**Do not use the `games.json` from this repository!**

Download the catalog yourself from [widgets.playgama.com](https://widgets.playgama.com):

1. Sign up / log in to your account
2. Select the games you want or download the entire catalog
3. Download the JSON file

The downloaded file contains your unique **CLID** in each `gameURL`:
```
"gameURL": "https://playgama.com/export/game/puzzle?clid=p_YOUR_UNIQUE_ID"
```

**CLID is required for:**
- 💰 Calculating your ad revenue
- 📊 Statistics in the partner program
- 💵 Receiving payouts

Without CLID, ad revenue will not be credited to your account!

---

## 📁 Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/playgama/games/
│   │   │   ├── models/
│   │   │   │   └── Game.kt                 # Data models for games
│   │   │   ├── services/
│   │   │   │   └── GameCatalogService.kt   # Loading and filtering service
│   │   │   └── ui/
│   │   │       ├── GameListActivity.kt     # Game catalog screen
│   │   │       ├── GameListAdapter.kt      # RecyclerView adapter
│   │   │       └── GameWebViewActivity.kt  # WebView for launching games
│   │   ├── res/
│   │   │   ├── layout/                     # XML layouts
│   │   │   ├── values/                     # Strings, colors, themes
│   │   │   └── drawable/                   # Graphics
│   │   ├── assets/
│   │   │   └── games.json                  # ⭐️ Game catalog (place here!)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 🚀 Quick Start

### 1. Create a new project in Android Studio

1. File → New → New Project
2. Select **Empty Views Activity**
3. Settings:
   - Name: `PlaygamaGames`
   - Package name: `com.playgama.games`
   - Language: **Kotlin**
   - Minimum SDK: **API 24** (Android 7.0)

### 2. Copy source code

1. Copy the contents of `java/com/playgama/games/` to your project
2. Copy the `res/` folder (layouts, values, drawable)
3. Update `AndroidManifest.xml`
4. Update `build.gradle.kts` (add dependencies)

### 3. Add games.json to assets

1. Create folder `app/src/main/assets/` (if it doesn't exist)
2. Copy `games.json` to this folder

```
app/
└── src/
    └── main/
        └── assets/
            └── games.json  ← here!
```

### 4. Sync Gradle and run

1. File → Sync Project with Gradle Files
2. Run → Run 'app'

---

## 📖 Component Descriptions

### Game.kt — Data Models

```kotlin
// Main game model
data class Game(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val gameURL: String,          // ⭐️ URL to load in WebView
    val images: List<String>,     // Preview images
    val genres: List<String>,     // Genres
    val mobileReady: List<String>?, // Platform compatibility
    val screenOrientation: ScreenOrientation?, // Screen orientation
    // ... other fields
)
```

### GameCatalogService.kt — Catalog Service

```kotlin
val service = GameCatalogService.getInstance(context)

// Load from assets
val games = service.loadGamesFromAssets()

// Load from server (in coroutine)
val games = service.loadGamesFromURL("https://...")

// Search and filter
val results = service.searchGames("puzzle", games)
val androidGames = service.filterAndroidCompatible(games)
```

### GameWebViewActivity.kt — Game Launch

```kotlin
// With Game object
val intent = GameWebViewActivity.createIntent(context, game)
startActivity(intent)

// Or directly with URL
val intent = GameWebViewActivity.createIntent(
    context,
    gameUrl = "https://playgama.com/export/game/puzzle-game",
    gameTitle = "Puzzle Game",
    orientation = "landscape"
)
startActivity(intent)
```

---

## ⚙️ Important WebView Settings

For games to work correctly in WebView:

```kotlin
webView.settings.apply {
    // 1. Enable JavaScript — REQUIRED!
    javaScriptEnabled = true
    
    // 2. DOM Storage — for saving progress
    domStorageEnabled = true
    
    // 3. Disable gesture requirement for media
    mediaPlaybackRequiresUserGesture = false
    
    // 4. Wide viewport for games
    loadWithOverviewMode = true
    useWideViewPort = true
}
```

---

## 💾 Game Progress Saving

Games save progress via **LocalStorage** and **IndexedDB**.

### What to enable

```kotlin
webView.settings.apply {
    // REQUIRED for saving progress!
    domStorageEnabled = true   // LocalStorage, SessionStorage
    databaseEnabled = true     // IndexedDB for large data
}
```

### Where data is stored

Data is saved in the app's private directory:
```
/data/data/com.playgama.games/app_webview/
├── Local Storage/          # LocalStorage
├── IndexedDB/              # IndexedDB
└── Cookies                 # Cookies
```

### Important to know

| Action | Progress |
|--------|----------|
| Close game and reopen | ✅ Saved |
| Restart app | ✅ Saved |
| Update app | ✅ Saved |
| Clear app data | ❌ Lost |
| Uninstall app | ❌ Lost |
| Different device | ❌ Not synced* |

*Some games may have their own cloud sync

---

## 📦 Dependencies

In `build.gradle.kts` add:

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // RecyclerView for game list
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // WebKit for additional WebView features
    implementation("androidx.webkit:webkit:1.10.0")
}
```

---

## 🔧 AndroidManifest.xml

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:usesCleartextTraffic="true"
    android:hardwareAccelerated="true">
    
    <!-- Game Activity with important settings -->
    <activity
        android:name=".ui.GameWebViewActivity"
        android:configChanges="orientation|screenSize|keyboardHidden"
        android:hardwareAccelerated="true"
        android:screenOrientation="sensor" />
        
</application>
```

---

## 📱 Requirements

- Android 7.0+ (API 24+)
- Android Studio Hedgehog (2023.1.1) or newer
- Kotlin 1.9+
- Gradle 8.2+

---

## 📞 Support

For integration questions: **partners@playgama.com**

Documentation: [wiki.playgama.com](https://wiki.playgama.com/playgama/for-partners/import-the-game-catalog)
