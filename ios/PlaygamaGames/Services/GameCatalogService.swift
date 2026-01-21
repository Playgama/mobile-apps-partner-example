// GameCatalogService.swift
// Service for loading and managing the game catalog

import Foundation

// MARK: - Errors

enum GameCatalogError: Error, LocalizedError {
    case fileNotFound
    case dataLoadFailed
    case parsingFailed(Error)
    case networkError(Error)
    
    var errorDescription: String? {
        switch self {
        case .fileNotFound:
            return "games.json file not found in the app bundle"
        case .dataLoadFailed:
            return "Failed to read data from file"
        case .parsingFailed(let error):
            return "JSON parsing error: \(error.localizedDescription)"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        }
    }
}

// MARK: - Game Catalog Service

class GameCatalogService {
    
    static let shared = GameCatalogService()
    
    private var cachedGames: [Game]?
    private var cachedSegments: [GameSegment]?
    
    // MARK: - Load from Bundle
    
    /// Loads the game catalog from a local JSON file (games.json)
    func loadGamesFromBundle() throws -> [Game] {
        if let cached = cachedGames {
            return cached
        }
        
        guard let fileURL = Bundle.main.url(forResource: "games", withExtension: "json") else {
            throw GameCatalogError.fileNotFound
        }
        
        let data: Data
        do {
            data = try Data(contentsOf: fileURL)
        } catch {
            throw GameCatalogError.dataLoadFailed
        }
        
        let catalog: GameCatalog
        do {
            let decoder = JSONDecoder()
            catalog = try decoder.decode(GameCatalog.self, from: data)
        } catch {
            throw GameCatalogError.parsingFailed(error)
        }
        
        let allGames = catalog.segments.flatMap { $0.hits }
        
        cachedGames = allGames
        cachedSegments = catalog.segments
        
        return allGames
    }
    
    /// Loads segments (categories) with games
    func loadSegmentsFromBundle() throws -> [GameSegment] {
        if let cached = cachedSegments {
            return cached
        }
        _ = try loadGamesFromBundle()
        return cachedSegments ?? []
    }
    
    // MARK: - Load from URL
    
    /// Loads the game catalog from a remote server
    func loadGamesFromURL(_ urlString: String) async throws -> [Game] {
        guard let url = URL(string: urlString) else {
            throw GameCatalogError.networkError(URLError(.badURL))
        }
        
        let (data, response) = try await URLSession.shared.data(from: url)
        
        if let httpResponse = response as? HTTPURLResponse {
            guard (200...299).contains(httpResponse.statusCode) else {
                throw GameCatalogError.networkError(URLError(.badServerResponse))
            }
        }
        
        do {
            let decoder = JSONDecoder()
            let catalog = try decoder.decode(GameCatalog.self, from: data)
            let allGames = catalog.segments.flatMap { $0.hits }
            
            cachedGames = allGames
            cachedSegments = catalog.segments
            
            return allGames
        } catch {
            throw GameCatalogError.parsingFailed(error)
        }
    }
    
    // MARK: - Filtering and Search
    
    /// Search games by title
    func searchGames(_ query: String, in games: [Game]) -> [Game] {
        guard !query.isEmpty else { return games }
        let lowercasedQuery = query.lowercased()
        
        return games.filter { game in
            game.title.lowercased().contains(lowercasedQuery) ||
            game.description.lowercased().contains(lowercasedQuery) ||
            game.genres.contains { $0.lowercased().contains(lowercasedQuery) }
        }
    }
    
    /// Filter games by genre
    func filterByGenre(_ genre: String, in games: [Game]) -> [Game] {
        return games.filter { game in
            game.genres.contains { $0.lowercased() == genre.lowercased() }
        }
    }
    
    /// Filter iOS compatible games
    func filterIOSCompatible(_ games: [Game]) -> [Game] {
        return games.filter { $0.supportsIOS }
    }
    
    /// Filter by screen orientation
    func filterByOrientation(landscape: Bool, in games: [Game]) -> [Game] {
        return games.filter { game in
            guard let orientation = game.screenOrientation else { return true }
            return landscape ? orientation.horizontal : orientation.vertical
        }
    }
    
    // MARK: - Cache Management
    
    func clearCache() {
        cachedGames = nil
        cachedSegments = nil
    }
}
