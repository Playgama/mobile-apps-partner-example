package com.playgama.games.ui

/**
 * Activity for displaying and playing a game.
 * 
 * Uses WebView with optimal settings for games:
 * - JavaScript enabled
 * - DOM Storage for progress saving
 * - Hardware acceleration
 * - Fullscreen video support
 */

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.playgama.games.R

class GameWebViewActivity : AppCompatActivity() {
    
    companion object {
        /** Key for passing game URL */
        const val EXTRA_GAME_URL = "extra_game_url"
        
        /** Key for passing game title */
        const val EXTRA_GAME_TITLE = "extra_game_title"
    }
    
    // ========================================================
    // UI ELEMENTS
    // ========================================================
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var closeButton: ImageButton
    private lateinit var gameTitleTextView: TextView
    
    /** Container for fullscreen video */
    private var fullscreenContainer: FrameLayout? = null
    
    /** Custom view for fullscreen video */
    private var customView: View? = null
    
    /** Callback for exiting fullscreen */
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    
    // ========================================================
    // DATA
    // ========================================================
    
    private var gameUrl: String = ""
    private var gameTitle: String = ""
    
    // ========================================================
    // LIFECYCLE
    // ========================================================
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_webview)
        
        // Get data from Intent
        gameUrl = intent.getStringExtra(EXTRA_GAME_URL) ?: ""
        gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: ""
        
        if (gameUrl.isEmpty()) {
            finish()
            return
        }
        
        // Initialize UI
        initViews()
        setupToolbar()
        setupWebView()
        
        // Load game
        loadGame()
    }
    
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }
    
    override fun onDestroy() {
        // Important: clean up WebView to free memory
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
    
    // ========================================================
    // INITIALIZATION
    // ========================================================
    
    private fun initViews() {
        webView = findViewById(R.id.gameWebView)
        progressBar = findViewById(R.id.loadingProgressBar)
        errorContainer = findViewById(R.id.errorContainer)
        errorText = findViewById(R.id.errorText)
        toolbar = findViewById(R.id.toolbar)
        closeButton = findViewById(R.id.closeButton)
        gameTitleTextView = findViewById(R.id.gameTitleTextView)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        
        // Retry button
        findViewById<android.widget.Button>(R.id.retryButton).setOnClickListener {
            loadGame()
        }
    }
    
    private fun setupToolbar() {
        gameTitleTextView.text = gameTitle
        
        closeButton.setOnClickListener {
            finish()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // Basic settings
            // JavaScript MUST be enabled for games
            javaScriptEnabled = true
            
            // DOM Storage for saving progress
            // This enables localStorage and sessionStorage
            domStorageEnabled = true
            
            // Database for IndexedDB
            // Some games use IndexedDB for progress
            databaseEnabled = true
            
            // Cache for resources
            cacheMode = WebSettings.LOAD_DEFAULT
            
            // Media settings
            // Allow video/audio to play without user action
            mediaPlaybackRequiresUserGesture = false
            
            // Allow mixed HTTP/HTTPS content
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Enable scaling
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // Optimization settings
            // Enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        
        // Disable overscroll effect to prevent accidental back navigation
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.isHorizontalScrollBarEnabled = false
        
        // Set WebViewClient to handle page loading
        webView.webViewClient = GameWebViewClient()
        
        // Set WebChromeClient for fullscreen video
        webView.webChromeClient = GameWebChromeClient()
    }
    
    // ========================================================
    // GAME LOADING
    // ========================================================
    
    private fun loadGame() {
        // Show loading
        showLoading()
        
        // Load URL
        webView.loadUrl(gameUrl)
    }
    
    // ========================================================
    // UI STATES
    // ========================================================
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }
    
    private fun showContent() {
        progressBar.visibility = View.GONE
        errorContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        webView.visibility = View.GONE
        errorText.text = message
    }
    
    // ========================================================
    // BACK BUTTON HANDLING
    // ========================================================
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            // If in fullscreen - exit fullscreen
            customView != null -> {
                customViewCallback?.onCustomViewHidden()
                customView = null
            }
            // If can go back in WebView - go back
            webView.canGoBack() -> {
                webView.goBack()
            }
            // Otherwise close activity
            else -> {
                @Suppress("DEPRECATION")
                super.onBackPressed()
            }
        }
    }
    
    // ========================================================
    // WEB VIEW CLIENT
    // ========================================================
    
    /**
     * WebViewClient handles page loading events.
     */
    private inner class GameWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showLoading()
        }
        
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            showContent()
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            
            // Show error only for main page
            if (request?.isForMainFrame == true) {
                val errorMessage = when (error?.errorCode) {
                    ERROR_HOST_LOOKUP -> getString(R.string.error_no_internet)
                    ERROR_TIMEOUT -> getString(R.string.error_timeout)
                    else -> getString(R.string.error_loading_game)
                }
                showError(errorMessage)
            }
        }
        
        // Allow all URL schemes (http, https, etc.)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }
    }
    
    // ========================================================
    // WEB CHROME CLIENT
    // ========================================================
    
    /**
     * WebChromeClient handles Chrome-specific events:
     * - Loading progress
     * - Fullscreen video
     * - JavaScript dialogs
     */
    private inner class GameWebChromeClient : WebChromeClient() {
        
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            
            // Update progress bar
            this@GameWebViewActivity.progressBar.progress = newProgress
            
            // Hide progress bar when fully loaded
            if (newProgress >= 100) {
                this@GameWebViewActivity.progressBar.visibility = View.GONE
            }
        }
        
        // Fullscreen video
        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            
            if (customView != null) {
                callback?.onCustomViewHidden()
                return
            }
            
            customView = view
            customViewCallback = callback
            
            // Hide main content
            webView.visibility = View.GONE
            toolbar.visibility = View.GONE
            
            // Show fullscreen
            fullscreenContainer?.apply {
                visibility = View.VISIBLE
                addView(view)
            }
            
            // Landscape orientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        
        override fun onHideCustomView() {
            super.onHideCustomView()
            
            // Remove fullscreen view
            fullscreenContainer?.apply {
                removeAllViews()
                visibility = View.GONE
            }
            
            customView = null
            customViewCallback = null
            
            // Show main content
            webView.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
            
            // Return to sensor-based orientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
}
