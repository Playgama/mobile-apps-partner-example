// GameWebView.swift
// WebView for launching Playgama games

import SwiftUI
import WebKit

// MARK: - SwiftUI WebView Wrapper

struct GameWebView: UIViewRepresentable {
    
    let gameURL: String
    @Binding var isLoading: Bool
    @Binding var loadingError: String?
    
    init(game: Game, isLoading: Binding<Bool> = .constant(true), loadingError: Binding<String?> = .constant(nil)) {
        self.gameURL = game.gameURL
        self._isLoading = isLoading
        self._loadingError = loadingError
    }
    
    init(gameURL: String, isLoading: Binding<Bool> = .constant(true), loadingError: Binding<String?> = .constant(nil)) {
        self.gameURL = gameURL
        self._isLoading = isLoading
        self._loadingError = loadingError
    }
    
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        
        // Important settings for games
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        configuration.defaultWebpagePreferences.allowsContentJavaScript = true
        configuration.allowsAirPlayForMediaPlayback = true
        
        // Allow opening links in the same WebView (important for game navigation)
        configuration.preferences.javaScriptCanOpenWindowsAutomatically = true
        
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator  // Important for JS alerts and popups
        
        // Allow scrolling inside games (some games need it)
        webView.scrollView.isScrollEnabled = true
        webView.scrollView.bounces = false
        
        // Visual settings
        webView.isOpaque = true
        webView.backgroundColor = .black
        webView.scrollView.backgroundColor = .black
        webView.scrollView.showsVerticalScrollIndicator = false
        webView.scrollView.showsHorizontalScrollIndicator = false
        
        // Allow user interaction
        webView.isUserInteractionEnabled = true
        webView.scrollView.isUserInteractionEnabled = true
        
        #if DEBUG
        if #available(iOS 16.4, *) {
            webView.isInspectable = true
        }
        #endif
        
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        guard let url = URL(string: gameURL) else {
            loadingError = "Invalid game URL"
            return
        }
        
        if webView.url?.absoluteString != gameURL {
            let request = URLRequest(url: url)
            webView.load(request)
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        var parent: GameWebView
        
        init(_ parent: GameWebView) {
            self.parent = parent
        }
        
        // MARK: - WKNavigationDelegate
        
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            parent.isLoading = false
            parent.loadingError = nil
        }
        
        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            parent.isLoading = false
            parent.loadingError = error.localizedDescription
        }
        
        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            parent.isLoading = false
            parent.loadingError = error.localizedDescription
        }
        
        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            parent.isLoading = true
            parent.loadingError = nil
        }
        
        // Allow all navigation (important for game redirects after ads)
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            decisionHandler(.allow)
        }
        
        // MARK: - WKUIDelegate
        
        // Handle JavaScript alert()
        func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
            completionHandler()
        }
        
        // Handle JavaScript confirm()
        func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
            completionHandler(true)
        }
        
        // Handle window.open() - create new WebView in same frame
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            // Load the URL in the same WebView instead of opening new window
            if let url = navigationAction.request.url {
                webView.load(URLRequest(url: url))
            }
            return nil
        }
    }
}

// MARK: - Game Player View

struct GamePlayerView: View {
    
    let game: Game
    @Environment(\.dismiss) private var dismiss
    @State private var isLoading = true
    @State private var loadingError: String?
    
    var body: some View {
        VStack(spacing: 0) {
            // Top bar with Exit button
            HStack {
                Button {
                    dismiss()
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                        Text("Exit")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color.white.opacity(0.2))
                    .cornerRadius(8)
                }
                
                Spacer()
                
                Text(game.title)
                    .font(.headline)
                    .foregroundColor(.white)
                    .lineLimit(1)
                
                Spacer()
                
                Color.clear
                    .frame(width: 70, height: 1)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.black)
            
            // Content: WebView or loading/error state
            ZStack {
                Color.black
                
                GameWebView(
                    game: game,
                    isLoading: $isLoading,
                    loadingError: $loadingError
                )
                
                if isLoading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .scaleEffect(1.5)
                            .tint(.white)
                        
                        Text("Loading game...")
                            .foregroundColor(.white)
                            .font(.headline)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black)
                }
                
                if let error = loadingError {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.orange)
                        
                        Text("Loading Error")
                            .font(.headline)
                            .foregroundColor(.white)
                        
                        Text(error)
                            .font(.subheadline)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        
                        Button("Close") {
                            dismiss()
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black)
                }
            }
        }
        .background(Color.black)
        .ignoresSafeArea(.container, edges: .bottom)
        .statusBarHidden(false)
    }
}

#Preview {
    let testGame = Game(
        id: "1",
        slug: "test-game",
        title: "Test Game",
        description: "A test game for preview",
        howToPlayText: nil,
        gameURL: "https://playgama.com/export/game/popping-candies",
        playgamaGameUrl: "https://playgama.com/game/popping-candies",
        images: [],
        videos: nil,
        genres: ["puzzle"],
        tags: nil,
        mobileReady: ["For IOS"],
        supportedLanguages: nil,
        screenOrientation: ScreenOrientation(horizontal: true, vertical: false),
        gender: nil,
        inGamePurchases: nil,
        embed: nil
    )
    
    return GamePlayerView(game: testGame)
}
