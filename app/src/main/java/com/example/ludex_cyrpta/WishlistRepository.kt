package com.example.ludex_cyrpta

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WishlistRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    // Initialize Local DB
    private val gameDao = AppDatabase.getDatabase(context).gameDao()
    private val TAG = "WishlistRepository"

    //Add to Wishlist
    fun addGameToWishlist(game: Game, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // Save Locally
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Set inWishlist = true: ensures that the game is saved visually even in offline app usage
                val localGame = game.toLocalGame().apply { inWishlist = true }
                gameDao.insertGame(localGame)
                Log.d(TAG, "Saved to local Wishlist DB")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save locally: ${e.message}")
            }
        }
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        // Save to: users -> [ID] -> wishlist -> [GameID]
        val wishRef = db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .document(game.id.toString())

        val gameData = hashMapOf(
            "id" to game.id,
            "name" to game.name,
            "imageLink" to game.imageLink,
            "rating" to game.rating,
            "releaseDate" to game.releaseDate,
            "synopsis" to game.synopsis,
            "added_at" to FieldValue.serverTimestamp()
        )

        wishRef.set(gameData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error adding to wishlist") }
    }

    //  Remove from Wishlist
    fun removeGameFromWishlist(gameId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // Remove Locally: We update the flag to false instead of deleting the whole row, in case it's also in the Vault
        CoroutineScope(Dispatchers.IO).launch {
            val game = gameDao.getGameById(gameId)
            if (game != null) {
                game.inWishlist = false
                gameDao.insertGame(game) // Update record
            }
        }

        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("wishlist")
                .document(gameId.toString())
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Error removing from wishlist") }
        }
    }


    // Check status
    fun isGameInWishlist(gameId: Int, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return
        db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .document(gameId.toString())
            .get()
            .addOnSuccessListener { document -> onResult(document.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // Get all Wishlist games
    fun getWishlistGames(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        // Try Local First
        CoroutineScope(Dispatchers.IO).launch {
            val localGames = gameDao.getWishlistGames()
            if (localGames.isNotEmpty()) {
                // If we have data, show it
                val games = localGames.map { it.toGame() }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(games)
                }
            } else {
                // If Local is empty, fetch from Cloud (existing logic)
                fetchFromCloud(onResult, onFailure)
            }
        }

    }

    private fun fetchFromCloud(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .orderBy("added_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val gameList = mutableListOf<Game>()
                for (document in result) {
                    try {
                        val game = Game(
                            id = document.getLong("id")?.toInt() ?: 0,
                            name = document.getString("name") ?: "Unknown",
                            rating = document.getDouble("rating") ?: 0.0,
                            imageLink = document.getString("imageLink") ?: "",
                            releaseDate = document.getString("releaseDate") ?: "n/a",
                            synopsis = document.getString("synopsis") ?: ""
                        )
                        gameList.add(game)
                        // Save these to local DB now for next time
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing game", e)
                    }
                }
                onResult(gameList)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error fetching wishlist") }
    }

    private fun Game.toLocalGame(): LocalGame {  //local  offline save
        return LocalGame(
            id = this.id,
            name = this.name,
            rating = this.rating,
            imageLink = this.imageLink,
            releaseDate = this.releaseDate,
            descr = this.descr,
            synopsis = this.synopsis,
            trailerLink = this.trailerLink,
            website = this.website,
            genreTag = this.genreTag,
            themeTag = this.themeTag,
            gameModeTag = this.gameModeTag,
            platformTag = this.platformTag,
            otherServicesTag = this.otherServicesTag
        )
    }

    private fun LocalGame.toGame(): Game { // online
        return Game(
            id = this.id,
            name = this.name,
            rating = this.rating,
            imageLink = this.imageLink,
            genreTag = this.genreTag,
            themeTag = this.themeTag,
            gameModeTag = this.gameModeTag,
            platformTag = this.platformTag,
            otherServicesTag = this.otherServicesTag,
            releaseDate = this.releaseDate,
            trailerLink = this.trailerLink,
            descr = this.descr,
            synopsis = this.synopsis,
            listBelong = emptyMap(),
            trending = false,
            website = this.website
        )
    }


}