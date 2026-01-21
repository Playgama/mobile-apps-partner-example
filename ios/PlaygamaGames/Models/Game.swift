// Game.swift
// Data models for games from the Playgama catalog

import Foundation

// MARK: - Root JSON Structure

/// Root structure of the JSON game catalog file
struct GameCatalog: Codable {
    let segments: [GameSegment]
}

// MARK: - Segment (Game Category)

/// A segment is a category or collection of games
struct GameSegment: Codable {
    let title: String
    let count: Int
    let hits: [Game]
}

// MARK: - Game Model

/// Main game model from the Playgama catalog
struct Game: Codable, Identifiable {
    
    // MARK: Identification
    let id: String
    let slug: String
    let title: String
    
    // MARK: Description
    let description: String
    let howToPlayText: String?
    
    // MARK: URLs
    
    /// Main URL to load the game in WebView
    let gameURL: String
    let playgamaGameUrl: String
    
    // MARK: Media
    let images: [String]
    let videos: [GameVideo]?
    
    // MARK: Categories
    let genres: [String]
    let tags: [String]?
    
    // MARK: Compatibility
    let mobileReady: [String]?
    let supportedLanguages: [String]?
    let screenOrientation: ScreenOrientation?
    
    // MARK: Additional Info
    let gender: [String]?
    let inGamePurchases: String?
    let embed: String?
}

// MARK: - Supporting Structures

struct GameVideo: Codable {
    let playgama_id: String?
    let external_url: String?
    let type: String?
}

struct ScreenOrientation: Codable {
    let horizontal: Bool
    let vertical: Bool
}

// MARK: - Convenience Extensions

extension Game {
    
    var thumbnailURL: URL? {
        guard let firstImage = images.first else { return nil }
        return URL(string: firstImage)
    }
    
    var gamePlayURL: URL? {
        return URL(string: gameURL)
    }
    
    var supportsIOS: Bool {
        return mobileReady?.contains("For IOS") ?? false
    }
    
    var supportsAndroid: Bool {
        return mobileReady?.contains("For Android") ?? false
    }
    
    var preferredOrientation: String {
        guard let orientation = screenOrientation else { return "any" }
        if orientation.horizontal && !orientation.vertical {
            return "landscape"
        } else if orientation.vertical && !orientation.horizontal {
            return "portrait"
        }
        return "any"
    }
    
    var genresDisplayText: String {
        let displayGenres = genres.prefix(3)
        return displayGenres.joined(separator: " • ")
    }
}
