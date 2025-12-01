package com.example.ludex_cyrpta

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GameDetailsFragment : Fragment(R.layout.game_details) {

    private var currentGame: Game? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Retrieve the Game object passed from the previous screen
        // We assume we passed it via arguments using the key "SELECTED_GAME"
        arguments?.let {
            currentGame = it.getSerializable("SELECTED_GAME") as? Game
        }

        // 2. Bind UI Elements
        val imgView = view.findViewById<ImageView>(R.id.detailImage)
        val titleView = view.findViewById<TextView>(R.id.detailTitle)
        val dateView = view.findViewById<TextView>(R.id.detailDate)
        val descView = view.findViewById<TextView>(R.id.detailDescription)
        val addBtn = view.findViewById<Button>(R.id.btnAddToList)

        // 3. Display Data
        currentGame?.let { game ->
            titleView.text = game.name
            dateView.text = "Released: ${game.releaseDate}"
            descView.text = game.descr

            // Use Glide for the image (same as your Adapter)
            Glide.with(this)
                .load(game.imageLink)
                .placeholder(R.drawable.ic_launcher_background) // placeholder
                .into(imgView)

            // 4. Handle "Add to Vault" Click
            addBtn.setOnClickListener {
                saveGameToLibrary(game)
            }
        }
    }

    private fun saveGameToLibrary(game: Game) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Please login to save games", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Save Locally (Room)
                saveToRoom(game)

                // Save to Cloud (Firebase)
                saveToFirebase(user.uid, game)

                Toast.makeText(context, "${game.name} saved to Vault!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveToRoom(game: Game) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            database.gameDao().insertGame(game)
        }
    }

    private suspend fun saveToFirebase(userId: String, game: Game) {
        // Saves the game under users -> [userID] -> savedGames -> [gameID]
        db.collection("users")
            .document(userId)
            .collection("savedGames")
            .document(game.id.toString())
            .set(game.toMap())
            .await()
    }

    companion object {
        // Helper to create the fragment with arguments
        fun newInstance(game: Game): GameDetailsFragment {
            val fragment = GameDetailsFragment()
            val args = Bundle()
            args.putSerializable("SELECTED_GAME", game)
            fragment.arguments = args
            return fragment
        }
    }
}