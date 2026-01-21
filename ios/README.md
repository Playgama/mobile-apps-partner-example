# Playgama Games — iOS Integration

Example of integrating the Playgama game catalog into an iOS app using SwiftUI.

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
ios/
├── PlaygamaGames/
│   ├── Models/
│   │   └── Game.swift              # Data models for games
│   ├── Services/
│   │   └── GameCatalogService.swift # Loading and filtering service
│   ├── Views/
│   │   ├── GameListView.swift      # Game catalog screen
│   │   └── GameWebView.swift       # WebView for launching games
│   └── PlaygamaGamesApp.swift      # App entry point
├── games.json                       # Game catalog
└── README.md                        # This documentation
```

## 🚀 Quick Start

### 1. Create a new project in Xcode

1. Open Xcode → File → New → Project
2. Select **iOS → App**
3. Settings:
   - Product Name: `PlaygamaGames`
   - Interface: **SwiftUI**
   - Language: **Swift**

### 2. Add files to the project

1. Copy all `.swift` files from the `PlaygamaGames/` folder to your project
2. Add `games.json` to the project:
   - Drag the file into Xcode
   - ✅ Make sure **Copy items if needed** is checked
   - ✅ Make sure your Target is selected in **Add to targets**

### 3. Configure Info.plist

Add permission to load content from the network:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

### 4. Run the app

Press ▶️ (Cmd + R) to run on a simulator or device.

---

## 📖 Component Descriptions

### Game.swift — Data Models

```swift
// Main game model
struct Game: Codable, Identifiable {
    let id: String
    let slug: String
    let title: String
    let description: String
    let gameURL: String          // ⭐️ URL to load in WebView
    let images: [String]         // Preview images
    let genres: [String]         // Genres
    let mobileReady: [String]?   // Platform compatibility
    let screenOrientation: ScreenOrientation? // Screen orientation
    // ... other fields
}
```

### GameCatalogService.swift — Catalog Service

```swift
let service = GameCatalogService.shared

// Load from local file
let games = try service.loadGamesFromBundle()

// Load from server
let games = try await service.loadGamesFromURL("https://...")

// Search and filter
let results = service.searchGames("puzzle", in: games)
let iosGames = service.filterIOSCompatible(games)
```

### GameWebView.swift — Game Launch

```swift
// In SwiftUI View
GameWebView(game: selectedGame)

// Or directly with URL
GameWebView(gameURL: "https://playgama.com/export/game/puzzle-game")
```

---

## ⚙️ Important WebView Settings

For games to work correctly in `WKWebView`:

```swift
let configuration = WKWebViewConfiguration()

// 1. Allow inline media playback
configuration.allowsInlineMediaPlayback = true

// 2. Disable gesture requirement for playback
configuration.mediaTypesRequiringUserActionForPlayback = []

// 3. Enable JavaScript
configuration.defaultWebpagePreferences.allowsContentJavaScript = true
```

---

## 💾 Game Progress Saving

Games save progress via **LocalStorage** and **IndexedDB**.

WKWebView uses `WKWebsiteDataStore.default()` by default, which automatically saves:
- LocalStorage
- IndexedDB
- Cookies

```swift
// ✅ CORRECT — progress is saved (default)
let configuration = WKWebViewConfiguration()
// websiteDataStore = .default() — already set

// ❌ WRONG — progress is lost on close!
configuration.websiteDataStore = .nonPersistent()
```

---

## 📱 Requirements

- iOS 15.0+
- Xcode 14.0+
- Swift 5.5+

---

## 📞 Support

For integration questions: **partners@playgama.com**

Documentation: [wiki.playgama.com](https://wiki.playgama.com/playgama/for-partners/import-the-game-catalog)
