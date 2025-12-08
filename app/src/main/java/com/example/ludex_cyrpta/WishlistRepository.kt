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
import kotlinx.coroutines.withContext

class WishlistRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Initialize Local DB
    private val gameDao = AppDatabase.getDatabase(context).gameDao()
    private val TAG = "WishlistRepository"

    // 1. Add to Wishlist
    fun addGameToWishlist(game: Game, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // A. Save Locally
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Set inWishlist = true
                val localGame = game.toLocalGame().apply { inWishlist = true }
                gameDao.insertGame(localGame)
                Log.d(TAG, "Saved to local Wishlist DB")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save locally: ${e.message}")
            }
        }

        // B. Save to Cloud
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

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
            "description" to game.descr,
            "trailerLink" to game.trailerLink,
            "website" to game.website,
            "added_at" to FieldValue.serverTimestamp(),
            "genres" to game.genreTag,
            "themes" to game.themeTag,
            "modes" to game.gameModeTag,
            "platforms" to game.platformTag,
            "services" to game.otherServicesTag
        )

        wishRef.set(gameData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error adding to wishlist") }
    }

    // 2. Check Status (Offline First)
    fun isGameInWishlist(gameId: Int, onResult: (Boolean) -> Unit) {
        // A. Check Local DB
        CoroutineScope(Dispatchers.IO).launch {
            val localGame = gameDao.getGameById(gameId)
            val isLocallySaved = localGame != null && localGame.inWishlist

            withContext(Dispatchers.Main) {
                if (isLocallySaved) {
                    onResult(true)
                } else {
                    // B. Check Cloud
                    checkFirestoreStatus(gameId, onResult)
                }
            }
        }
    }

    private fun checkFirestoreStatus(gameId: Int, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return
        db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .document(gameId.toString())
            .get()
            .addOnSuccessListener { document -> onResult(document.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // 3. Get Full List (Offline First + Sync)
    fun getWishlistGames(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        // A. Try Local First
        CoroutineScope(Dispatchers.IO).launch {
            val localList = gameDao.getWishlistGames()

            if (localList.isNotEmpty()) {
                val games = localList.map { it.toGame() }
                withContext(Dispatchers.Main) {
                    onResult(games)
                }
            } else {
                // B. Fetch from Cloud
                fetchFromCloud(onResult, onFailure)
            }
        }
    }

    private fun fetchFromCloud(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            CoroutineScope(Dispatchers.Main).launch { onFailure("No user logged in") }
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .orderBy("added_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val gameList = mutableListOf<Game>()
                for (document in result) {
                    try {
                        // Cast Firestore Arrays back to Kotlin Lists safely
                        // "If the field exists, use it. If not, use empty list."
                        val genres = (document.get("genres") as? List<String>) ?: emptyList()
                        val themes = (document.get("themes") as? List<String>) ?: emptyList()
                        val modes = (document.get("modes") as? List<String>) ?: emptyList()
                        val platforms = (document.get("platforms") as? List<String>) ?: emptyList()
                        val services = (document.get("services") as? List<String>) ?: emptyList()

                        val game = Game(
                            id = document.getLong("id")?.toInt() ?: 0,
                            name = document.getString("name") ?: "Unknown",
                            rating = document.getDouble("rating") ?: 0.0,
                            imageLink = document.getString("imageLink") ?: "",
                            releaseDate = document.getString("releaseDate") ?: "n/a",
                            synopsis = document.getString("synopsis") ?: "",
                            descr = document.getString("description") ?: "",
                            trailerLink = document.getString("trailerLink") ?: "",
                            website = document.getString("website") ?: "",

                            // Use the variables we extracted above
                            genreTag = genres,
                            themeTag = themes,
                            gameModeTag = modes,
                            platformTag = platforms,
                            otherServicesTag = services,

                            // These two usually don't need saving/loading for Vault logic
                            listBelong = emptyMap(),
                            trending = false
                        )
                        gameList.add(game)

                        // --- SYNC: Save to Local DB ---
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                gameDao.insertGame(game.toLocalGame().apply { inWishlist = true })
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to cache game", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing game", e)
                    }
                }
                onResult(gameList)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error fetching wishlist") }
    }

    // 4. Remove Game
    fun removeGameFromWishlist(gameId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // A. Update Local DB
        CoroutineScope(Dispatchers.IO).launch {
            val localGame = gameDao.getGameById(gameId)
            if (localGame != null) {
                localGame.inWishlist = false // Uncheck flag
                gameDao.insertGame(localGame)
            }
        }

        // B. Remove from Cloud
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

    // --- Helpers ---
    private fun Game.toLocalGame(): LocalGame {
        return LocalGame(
            id = this.id, name = this.name, rating = this.rating,
            imageLink = this.imageLink, releaseDate = this.releaseDate,
            descr = this.descr, synopsis = this.synopsis,
            trailerLink = this.trailerLink, website = this.website,
            genreTag = this.genreTag, themeTag = this.themeTag,
            gameModeTag = this.gameModeTag, platformTag = this.platformTag,
            otherServicesTag = this.otherServicesTag
        )
    }

    private fun LocalGame.toGame(): Game {
        return Game(
            id = this.id, name = this.name, rating = this.rating,
            imageLink = this.imageLink, genreTag = this.genreTag,
            themeTag = this.themeTag, gameModeTag = this.gameModeTag,
            platformTag = this.platformTag, otherServicesTag = this.otherServicesTag,
            releaseDate = this.releaseDate, trailerLink = this.trailerLink,
            descr = this.descr, synopsis = this.synopsis,
            listBelong = emptyMap(), trending = false, website = this.website
        )
    }
}