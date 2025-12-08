package com.example.ludex_cyrpta

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale

class TrendingGameAdapter(
    private val onItemClicked: (Game) -> Unit
) : ListAdapter<Game, TrendingGameAdapter.TrendingGameViewHolder>(TrendingGameDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingGameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trending_game_rv_item, parent, false)
        return TrendingGameViewHolder(view)
    }

    inner class TrendingGameViewHolder(val trView: View) : RecyclerView.ViewHolder(trView) {
        val gameImage: ImageView = trView.findViewById(R.id.image)
        val gameName: TextView = trView.findViewById(R.id.name)
        val gameRating: TextView = trView.findViewById(R.id.rating)
        val gameDebutDate: TextView = trView.findViewById(R.id.releaseDate)
        val gameStory: TextView = trView.findViewById(R.id.storySummary)
        val orderNum: TextView = trView.findViewById(R.id.placementNum)

        init {
            trView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onItemClicked(getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(holder: TrendingGameViewHolder, position: Int) {
        val game = getItem(position)
        holder.gameName.text = game.name
        holder.gameRating.text = game.rating.toString() //gameRating was already specified in GameViewModel
        holder.gameStory.text = game.synopsis
        holder.gameDebutDate.text = game.releaseDate

        val imageURL = game.imageLink
        val imagePlaceholder = android.R.drawable.ic_menu_gallery //placeholder if link doesn't work or is empty

        val radii = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 12f, holder.trView.context.resources.displayMetrics).toInt()

        val glideContext = Glide.with(holder.trView.context) //make it external to make the if/else statement more efficient
        val glideReq = if (imageURL.isNotEmpty()) {
            glideContext
                .load(imageURL)
                .transform(CenterCrop(), RoundedCorners(radii)) //give each image rounded corners
                .placeholder(imagePlaceholder)
                .error(imagePlaceholder) //if there's an error, use placeholder
        } else {
            glideContext
                .load(imagePlaceholder)
                .centerCrop()
        }
        glideReq.into(holder.gameImage)

        //change from regular Adapter: ordering
        holder.orderNum.text = if (position >= 0 && position < 9) {
            String.format(Locale.getDefault(), "0%d.", position + 1)
        } else {
            String.format(Locale.getDefault(), "%d.", position + 1)
        }
    }
}

class TrendingGameDiffCallback : DiffUtil.ItemCallback<Game>() {
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem == newItem
    }
}