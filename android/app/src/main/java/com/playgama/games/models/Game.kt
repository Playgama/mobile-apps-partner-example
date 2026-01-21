package com.playgama.games.models

/**
 * Data models for games from the Playgama catalog.
 * 
 * The JSON catalog contains "segments" - game categories.
 * Each segment contains a list of games (hits).
 */

import com.google.gson.annotations.SerializedName

// ============================================================
// ROOT STRUCTURE
// ============================================================

/**
 * Root structure of the JSON catalog.
 * Contains a list of segments (categories).
 */
data class GameCatalog(
    val segments: List<GameSegment>
)

/**
 * A segment is a category or collection of games.
 * Example: "Popular games", "New", "Action", etc.
 */
data class GameSegment(
    val title: String,
    val count: Int,
    val hits: List<Game>
)

// ============================================================
// MAIN GAME MODEL
// ============================================================

/**
 * Main game model from the Playgama catalog.
 * 
 * Key fields:
 * - [gameURL] - main URL to load the game in WebView
 * - [images] - list of cover image URLs
 * - [genres] - list of game genres
 * - [mobileReady] - list of supported platforms
 */
data class Game(
    // Identification
    val id: String,
    val slug: String,
    val title: String,
    
    // Description
    val description: String,
    
    @SerializedName("howToPlayText")
    val howToPlayText: String? = null,
    
    // URLs
    /** Main URL to load the game in WebView */
    val gameURL: String,
    
    val playgamaGameUrl: String,
    
    // Media
    /** List of cover image URLs */
    val images: List<String>,
    
    val videos: List<GameVideo>? = null,
    
    // Categories
    /** List of game genres (e.g., "action", "puzzle", "arcade") */
    val genres: List<String>,
    
    val tags: List<String>? = null,
    
    // Compatibility
    /** Supported platforms (e.g., "For Android", "For IOS", "For Desktop") */
    val mobileReady: List<String>? = null,
    
    val supportedLanguages: List<String>? = null,
    
    /** Supported screen orientations */
    val screenOrientation: ScreenOrientation? = null,
    
    // Additional info
    val gender: List<String>? = null,
    val inGamePurchases: String? = null,
    val embed: String? = null
) {
    // ========================================================
    // CONVENIENCE PROPERTIES
    // ========================================================
    
    /** Returns the first image URL or null */
    val thumbnailUrl: String?
        get() = images.firstOrNull()
    
    /** Returns true if the game supports Android */
    val supportsAndroid: Boolean
        get() = mobileReady?.contains("For Android") == true
    
    /** Returns true if the game supports iOS */
    val supportsIOS: Boolean
        get() = mobileReady?.contains("For IOS") == true
    
    /** Returns the preferred screen orientation */
    val preferredOrientation: String
        get() {
            val orientation = screenOrientation ?: return "any"
            return when {
                orientation.horizontal && !orientation.vertical -> "landscape"
                orientation.vertical && !orientation.horizontal -> "portrait"
                else -> "any"
            }
        }
    
    /** Returns formatted genres for display (first 3) */
    val genresDisplayText: String
        get() = genres.take(3).joinToString(" • ")
}

// ============================================================
// SUPPORTING STRUCTURES
// ============================================================

/**
 * Video information for the game.
 */
data class GameVideo(
    @SerializedName("playgama_id")
    val playgamaId: String? = null,
    
    @SerializedName("external_url")
    val externalUrl: String? = null,
    
    val type: String? = null
)

/**
 * Supported screen orientations for the game.
 */
data class ScreenOrientation(
    val horizontal: Boolean,
    val vertical: Boolean
)
