// GameListView.swift
// Game catalog screen

import SwiftUI

// MARK: - Main Game List View

struct GameListView: View {
    
    @State private var games: [Game] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var searchText = ""
    @State private var selectedGame: Game?
    
    private let catalogService = GameCatalogService.shared
    
    var filteredGames: [Game] {
        if searchText.isEmpty {
            return games
        }
        return catalogService.searchGames(searchText, in: games)
    }
    
    let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    var body: some View {
        NavigationView {
            ZStack {
                if isLoading {
                    loadingView
                } else if let error = errorMessage {
                    errorView(message: error)
                } else {
                    gamesListView
                }
            }
            .navigationTitle("Playgama Games")
            .searchable(text: $searchText, prompt: "Search games...")
            .fullScreenCover(item: $selectedGame) { game in
                GamePlayerView(game: game)
                    .interactiveDismissDisabled(true)
            }
        }
        .navigationViewStyle(.stack)
        .task {
            await loadGames()
        }
    }
    
    // MARK: - Subviews
    
    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading game catalog...")
                .foregroundColor(.secondary)
        }
    }
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text("Loading Error")
                .font(.headline)
            
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button("Retry") {
                Task {
                    await loadGames()
                }
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
    
    private var gamesListView: some View {
        ScrollView {
            HStack {
                Text("\(filteredGames.count) games")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                Spacer()
            }
            .padding(.horizontal)
            .padding(.top, 8)
            
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(filteredGames) { game in
                    GameCardView(game: game) {
                        selectedGame = game
                    }
                }
            }
            .padding()
        }
    }
    
    // MARK: - Methods
    
    private func loadGames() async {
        isLoading = true
        errorMessage = nil
        
        try? await Task.sleep(nanoseconds: 500_000_000)
        
        do {
            let loadedGames = try catalogService.loadGamesFromBundle()
            games = loadedGames
        } catch {
            errorMessage = error.localizedDescription
        }
        
        isLoading = false
    }
}

// MARK: - Game Card View

struct GameCardView: View {
    
    let game: Game
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 8) {
                
                AsyncImage(url: game.thumbnailURL) { phase in
                    switch phase {
                    case .empty:
                        Rectangle()
                            .fill(Color.gray.opacity(0.3))
                            .overlay { ProgressView() }
                        
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                        
                    case .failure:
                        Rectangle()
                            .fill(Color.gray.opacity(0.3))
                            .overlay {
                                Image(systemName: "gamecontroller.fill")
                                    .font(.largeTitle)
                                    .foregroundColor(.gray)
                            }
                        
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(height: 120)
                .clipped()
                .cornerRadius(12)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(game.title)
                        .font(.headline)
                        .foregroundColor(.primary)
                        .lineLimit(1)
                    
                    Text(game.genresDisplayText)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                .padding(.horizontal, 4)
            }
            .padding(8)
            .background(Color(.systemBackground))
            .cornerRadius(16)
            .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    GameListView()
}
