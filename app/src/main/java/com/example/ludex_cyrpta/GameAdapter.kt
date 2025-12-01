package com.example.ludex_cyrpta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GameAdapter(
    private val games: List<Game>,
    private val listener: OnListFragmentInteractionListener? // 1. Added Listener here
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Ensure this matches your XML file name for the row item
        val contactView = inflater.inflate(R.layout.game_rv_item, parent, false)
        return GameViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]

        // Set text views
        holder.gameName.text = game.name
        holder.gameDescr.text = game.descr
        // If your Game object doesn't have a price field, you might want to remove this line
        // or add a price field to your Game data class.
        // holder.gamePrice.text = game.price

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(game.imageLink)
            .placeholder(R.drawable.ic_launcher_background) // Add a placeholder if you have one
            .into(holder.gameImage)

        // 2. Set the click listener on the entire row
        holder.itemView.setOnClickListener {
            listener?.onItemClick(game)
        }
    }

    override fun getItemCount(): Int {
        return games.size
    }

    // Inner ViewHolder class
    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameName: TextView = itemView.findViewById(R.id.name)
        val gameDescr: TextView = itemView.findViewById(R.id.descr)
        val gamePrice: TextView = itemView.findViewById(R.id.price)
        val gameImage: ImageView = itemView.findViewById(R.id.image)
    }
}