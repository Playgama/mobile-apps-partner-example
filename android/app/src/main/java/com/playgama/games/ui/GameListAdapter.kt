package com.playgama.games.ui

/**
 * Adapter for displaying the game list in RecyclerView.
 * 
 * Uses DiffUtil for efficient list updates
 * and Coil for image loading.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.playgama.games.R
import com.playgama.games.models.Game

/**
 * Adapter for displaying games in grid.
 * 
 * @param onGameClick - click handler on game card
 */
class GameListAdapter(
    private val onGameClick: (Game) -> Unit
) : ListAdapter<Game, GameListAdapter.GameViewHolder>(GameDiffCallback()) {
    
    // ========================================================
    // VIEW HOLDER
    // ========================================================
    
    /**
     * ViewHolder for game card.
     * Caches view references for performance.
     */
    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.gameThumbnail)
        private val titleTextView: TextView = itemView.findViewById(R.id.gameTitle)
        private val genresTextView: TextView = itemView.findViewById(R.id.gameGenres)
        
        /**
         * Bind game data to views.
         */
        fun bind(game: Game) {
            // Game title
            titleTextView.text = game.title
            
            // Formatted genres
            genresTextView.text = game.genresDisplayText
            
            // Load image using Coil
            loadThumbnail(game)
            
            // Click handler
            itemView.setOnClickListener {
                onGameClick(game)
            }
        }
        
        /**
         * Load game cover image.
         * Uses Coil for caching and rounded corners.
         */
        private fun loadThumbnail(game: Game) {
            val imageUrl = game.thumbnailUrl
            
            if (imageUrl != null) {
                thumbnailImageView.load(imageUrl) {
                    // Placeholder while loading
                    placeholder(R.drawable.placeholder_game)
                    
                    // Image on error
                    error(R.drawable.placeholder_game)
                    
                    // Rounded corners
                    transformations(
                        RoundedCornersTransformation(
                            topLeft = 12f,
                            topRight = 12f,
                            bottomLeft = 0f,
                            bottomRight = 0f
                        )
                    )
                    
                    // Crossfade animation
                    crossfade(true)
                    crossfade(300)
                }
            } else {
                // No image - show placeholder
                thumbnailImageView.setImageResource(R.drawable.placeholder_game)
            }
        }
    }
    
    // ========================================================
    // ADAPTER METHODS
    // ========================================================
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// ============================================================
// DIFF CALLBACK
// ============================================================

/**
 * DiffUtil callback for efficient list comparison.
 * Allows RecyclerView to update only changed elements.
 */
class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
    
    /**
     * Check if items represent the same game.
     * Compare by unique ID.
     */
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }
    
    /**
     * Check if item contents changed.
     * Compare all fields (data class provides equals).
     */
    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem == newItem
    }
}
