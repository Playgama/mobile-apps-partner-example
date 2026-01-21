package com.playgama.games.services

/**
 * Service for loading and managing the game catalog.
 * 
 * Supports loading games from:
 * 1. Local assets file (games.json)
 * 2. Remote server (network URL)
 * 
 * Usage example:
 * ```kotlin
 * val service = GameCatalogService(context)
 * 
 * // Load from local file
 * val result = service.loadGamesFromAssets()
 * result.onSuccess { games ->
 *     // Display game list
 * }.onFailure { error ->
 *     // Handle error
 * }
 * ```
 */

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.playgama.games.models.Game
import com.playgama.games.models.GameCatalog
import com.playgama.games.models.GameSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class GameCatalogService(private val context: Context) {
    
    // ========================================================
    // PRIVATE FIELDS
    // ========================================================
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    /** Cache for loaded games */
    private var cachedGames: List<Game>? = null
    
    /** Cache for loaded segments */
    private var cachedSegments: List<GameSegment>? = null
    
    // ========================================================
    // LOADING FROM LOCAL FILE
    // ========================================================
    
    /**
     * Loads the game catalog from the assets/games.json file.
     * 
     * This is the recommended loading method - all games are
     * already bundled in the app, no network required.
     * 
     * @return Result with list of games or error
     */
    suspend fun loadGamesFromAssets(): Result<List<Game>> = withContext(Dispatchers.IO) {
        // Return cached data
        cachedGames?.let { 
            return@withContext Result.success(it) 
        }
        
        try {
            // Open file from assets
            val inputStream = context.assets.open("games.json")
            
            // Read all contents
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // Parse JSON
            val catalog = gson.fromJson(jsonString, GameCatalog::class.java)
            
            // Extract all games from all segments
            val allGames = catalog.segments.flatMap { it.hits }
            
            // Save to cache
            cachedGames = allGames
            cachedSegments = catalog.segments
            
            Result.success(allGames)
            
        } catch (e: IOException) {
            Result.failure(GameCatalogException.FileNotFound(
                "games.json file not found in assets. " +
                "Make sure the file is placed in app/src/main/assets/"
            ))
        } catch (e: Exception) {
            Result.failure(GameCatalogException.ParsingError(
                "Error parsing JSON: ${e.message}"
            ))
        }
    }
    
    /**
     * Loads segments (categories) from local file.
     */
    suspend fun loadSegmentsFromAssets(): Result<List<GameSegment>> = withContext(Dispatchers.IO) {
        cachedSegments?.let {
            return@withContext Result.success(it)
        }
        
        // First load games (which will also load segments)
        val gamesResult = loadGamesFromAssets()
        
        return@withContext gamesResult.map { 
            cachedSegments ?: emptyList() 
        }
    }
    
    // ========================================================
    // LOADING FROM NETWORK
    // ========================================================
    
    /**
     * Loads the game catalog from a remote server.
     * 
     * Use when you need to get updated catalog
     * without app update.
     * 
     * @param urlString - URL to JSON catalog file
     * @return Result with list of games or error
     */
    suspend fun loadGamesFromUrl(urlString: String): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 15000  // 15 seconds
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
            }
            
            // Check response code
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    GameCatalogException.NetworkError(
                        "Server error: ${connection.responseCode} ${connection.responseMessage}"
                    )
                )
            }
            
            // Read response
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            
            // Parse JSON
            val catalog = gson.fromJson(jsonString, GameCatalog::class.java)
            val allGames = catalog.segments.flatMap { it.hits }
            
            // Save to cache
            cachedGames = allGames
            cachedSegments = catalog.segments
            
            Result.success(allGames)
            
        } catch (e: IOException) {
            Result.failure(GameCatalogException.NetworkError(
                "Network error: ${e.message}. Check internet connection."
            ))
        } catch (e: Exception) {
            Result.failure(GameCatalogException.ParsingError(
                "Error parsing JSON: ${e.message}"
            ))
        }
    }
    
    // ========================================================
    // FILTERING AND SEARCH
    // ========================================================
    
    /**
     * Search games by title.
     * 
     * Search is case-insensitive and looks for matches
     * in title, description and genres.
     */
    fun searchGames(query: String, games: List<Game>): List<Game> {
        if (query.isBlank()) return games
        
        val lowercaseQuery = query.lowercase()
        
        return games.filter { game ->
            game.title.lowercase().contains(lowercaseQuery) ||
            game.description.lowercase().contains(lowercaseQuery) ||
            game.genres.any { it.lowercase().contains(lowercaseQuery) }
        }
    }
    
    /**
     * Filter games by genre.
     */
    fun filterByGenre(genre: String, games: List<Game>): List<Game> {
        return games.filter { game ->
            game.genres.any { it.equals(genre, ignoreCase = true) }
        }
    }
    
    /**
     * Filter Android compatible games only.
     */
    fun filterAndroidCompatible(games: List<Game>): List<Game> {
        return games.filter { it.supportsAndroid }
    }
    
    /**
     * Filter by screen orientation.
     * 
     * @param landscape - true for horizontal, false for vertical
     */
    fun filterByOrientation(landscape: Boolean, games: List<Game>): List<Game> {
        return games.filter { game ->
            val orientation = game.screenOrientation ?: return@filter true
            if (landscape) orientation.horizontal else orientation.vertical
        }
    }
    
    /**
     * Get unique list of all genres.
     */
    fun getAllGenres(games: List<Game>): List<String> {
        return games
            .flatMap { it.genres }
            .distinct()
            .sorted()
    }
    
    // ========================================================
    // CACHE MANAGEMENT
    // ========================================================
    
    /**
     * Clear cache.
     * Call when you need to reload data.
     */
    fun clearCache() {
        cachedGames = null
        cachedSegments = null
    }
    
    /**
     * Check if data is cached.
     */
    fun isCached(): Boolean = cachedGames != null
}

// ============================================================
// EXCEPTIONS
// ============================================================

/**
 * Exceptions for errors during game catalog operations.
 */
sealed class GameCatalogException(message: String) : Exception(message) {
    
    /** File not found in assets */
    class FileNotFound(message: String) : GameCatalogException(message)
    
    /** JSON parsing error */
    class ParsingError(message: String) : GameCatalogException(message)
    
    /** Network error */
    class NetworkError(message: String) : GameCatalogException(message)
}
