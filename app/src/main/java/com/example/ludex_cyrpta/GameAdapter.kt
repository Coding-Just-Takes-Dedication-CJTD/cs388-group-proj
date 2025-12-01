package com.example.ludex_cyrpta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.DiffUtil
import java.util.Locale

class GameAdapter(
    private val onItemClicked: (Game) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_rv_item, parent, false)
        return GameViewHolder(view)
    }

    inner class GameViewHolder(val gView: View) : RecyclerView.ViewHolder(gView) {
        val gameImage: ImageView = gView.findViewById(R.id.image)
        val gameName: TextView = gView.findViewById(R.id.name)
        val gameRating: TextView = gView.findViewById(R.id.rating)
        val gameDebutDate: TextView = gView.findViewById(R.id.releaseDate)
        val gameStory: TextView = gView.findViewById(R.id.storySummary)

        init {
            gView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onItemClicked(getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        holder.gameName.text = game.name
        holder.gameRating.text = game.rating.toString() //gameRating was already specified in GameViewModel
        holder.gameStory.text = game.synopsis
        holder.gameDebutDate.text = game.releaseDate

        val imageURL = game.imageLink
        val imagePlaceholder = android.R.drawable.ic_menu_gallery //placeholder if link doesn't work or is empty

        val glideContext = Glide.with(holder.gView.context) //make it external to make the if/else statement more efficient
        val glideReq = if (imageURL.isNotEmpty()) {
            glideContext
                .load(imageURL)
                .centerCrop()
                .placeholder(imagePlaceholder)
                .error(imagePlaceholder) //if there's an error, use placeholder
        } else {
            glideContext
                .load(imagePlaceholder)
                .centerCrop()
        }
        glideReq.into(holder.gameImage)
    }
}

class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem == newItem
    }
}