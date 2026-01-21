package com.playgama.games.ui

/**
 * Main screen with the list of games.
 * 
 * Features:
 * - Game list display in grid (2 columns)
 * - Search by title
 * - Loading and error states
 * - Navigation to game
 */

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.playgama.games.R
import com.playgama.games.models.Game
import com.playgama.games.services.GameCatalogService
import kotlinx.coroutines.launch

class GameListActivity : AppCompatActivity() {
    
    // ========================================================
    // UI ELEMENTS
    // ========================================================
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var loadingContainer: LinearLayout
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var gamesCountText: TextView
    
    // ========================================================
    // DATA
    // ========================================================
    
    private lateinit var catalogService: GameCatalogService
    private lateinit var adapter: GameListAdapter
    
    /** Full list of all games */
    private var allGames: List<Game> = emptyList()
    
    /** Current search query */
    private var currentSearchQuery: String = ""
    
    // ========================================================
    // LIFECYCLE
    // ========================================================
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)
        
        // Initialize service
        catalogService = GameCatalogService(this)
        
        // Initialize UI
        initViews()
        setupRecyclerView()
        setupSearch()
        
        // Load games
        loadGames()
    }
    
    // ========================================================
    // INITIALIZATION
    // ========================================================
    
    private fun initViews() {
        recyclerView = findViewById(R.id.gamesRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        loadingContainer = findViewById(R.id.loadingContainer)
        errorContainer = findViewById(R.id.errorContainer)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
        gamesCountText = findViewById(R.id.gamesCountText)
        
        // Retry button
        retryButton.setOnClickListener {
            loadGames()
        }
    }
    
    private fun setupRecyclerView() {
        // Create adapter with click handler
        adapter = GameListAdapter { game ->
            openGame(game)
        }
        
        // Grid with 2 columns
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        
        // Add spacing between elements
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(GridSpacingDecoration(2, spacing))
    }
    
    private fun setupSearch() {
        // React to search text changes
        searchEditText.doAfterTextChanged { text ->
            currentSearchQuery = text?.toString() ?: ""
            filterGames()
        }
    }
    
    // ========================================================
    // LOADING DATA
    // ========================================================
    
    private fun loadGames() {
        // Show loading
        showLoading()
        
        // Launch loading in coroutine
        lifecycleScope.launch {
            val result = catalogService.loadGamesFromAssets()
            
            result.onSuccess { games ->
                allGames = games
                filterGames()
                showContent()
                
            }.onFailure { error ->
                showError(error.message ?: getString(R.string.unknown_error))
            }
        }
    }
    
    private fun filterGames() {
        val filteredGames = if (currentSearchQuery.isBlank()) {
            allGames
        } else {
            catalogService.searchGames(currentSearchQuery, allGames)
        }
        
        adapter.submitList(filteredGames)
        updateGamesCount(filteredGames.size)
    }
    
    // ========================================================
    // UI STATES
    // ========================================================
    
    private fun showLoading() {
        loadingContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
        gamesCountText.visibility = View.GONE
    }
    
    private fun showContent() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        gamesCountText.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        gamesCountText.visibility = View.GONE
        errorText.text = message
    }
    
    private fun updateGamesCount(count: Int) {
        gamesCountText.text = resources.getQuantityString(
            R.plurals.games_count,
            count,
            count
        )
    }
    
    // ========================================================
    // NAVIGATION
    // ========================================================
    
    private fun openGame(game: Game) {
        val intent = Intent(this, GameWebViewActivity::class.java).apply {
            putExtra(GameWebViewActivity.EXTRA_GAME_URL, game.gameURL)
            putExtra(GameWebViewActivity.EXTRA_GAME_TITLE, game.title)
        }
        startActivity(intent)
    }
}

// ============================================================
// GRID SPACING DECORATION
// ============================================================

/**
 * Adds spacing between grid elements.
 */
class GridSpacingDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount
        
        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }
}
