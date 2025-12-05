package com.example.ludex_cyrpta

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context


class VaultRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val gameDao = AppDatabase.getDatabase(context).gameDao()
    private val TAG = "VaultRepository"

    // 1. Add a game to the user's personal vault
    fun addGameToVault(game: Game, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // A. Save to Local Room DB (Instant, works offline)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localGame = game.toLocalGame().apply { inVault = true }
                gameDao.insertGame(localGame)
                Log.d(TAG, "Saved to local Room DB")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save locally: ${e.message}")
            }
        }
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        // Firestore Path: users -> [USER_ID] -> vault -> [GAME_ID]
        // Game ID: avoid duplicate files for the same game
        val vaultRef = db.collection("users")
            .document(currentUser.uid)
            .collection("vault")
            .document(game.id.toString())

        // Save only the essential data needed to display the list later
        val gameData = hashMapOf(
            "id" to game.id,
            "name" to game.name,
            "imageLink" to game.imageLink,
            "rating" to game.rating,
            "releaseDate" to game.releaseDate,
            "synopsis" to game.synopsis,
            "added_at" to FieldValue.serverTimestamp() // Timestamp to sort by "newest added"
        )

        vaultRef.set(gameData)
            .addOnSuccessListener {
                Log.d(TAG, "Game added to firestore vault: ${game.name}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding game to Firestore", e)
                onFailure(e.message ?: "Unknown error")
            }
    }

    // if a game is already in the vault (used to update the button UI)
    fun isGameInVault(gameId: Int, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("vault")
            .document(gameId.toString())
            .get()
            .addOnSuccessListener { document ->
                // Returns true if the file exists, false if not
                onResult(document.exists())
            }
            .addOnFailureListener {
                // If checking fails (e.g. no internet), assume false so the app doesn't crash
                onResult(false)
            }
    }

    // full list of saved games for the Vault Fragment
    fun getVaultGames(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        // A. Check Local DB First
        CoroutineScope(Dispatchers.IO).launch {
            val localList = gameDao.getVaultGames()

            if (localList.isNotEmpty()) {
                Log.d(TAG, "Loaded ${localList.size} games from Local DB")
                val games = localList.map { it.toGame() }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(games)
                }
            } else {
                // B. If Local is empty, fetch from Firestore
                fetchFromFirestore(onResult, onFailure)
            }
        }
    }

    private fun fetchFromFirestore(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("No user logged in")
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("vault")
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

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing game: ${e.message}")
                    }
                }
                onResult(gameList)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error fetching vault") }
    }



    // Remove a game from the vault (Updates Local AND Cloud)
    fun removeGameFromVault(gameId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser

        // UPDATE LOCAL DATABASE
        CoroutineScope(Dispatchers.IO).launch {
            // Find the game in the backpack
            val localGame = gameDao.getGameById(gameId)

            // If found, uncheck the "inVault" flag
            if (localGame != null) {
                localGame.inVault = false


                // (REPLACE) to update the existing row.
                gameDao.insertGame(localGame)
                Log.d(TAG, "Removed from local Room DB (Flag updated)")
            }
        }

        // REMOVE FROM FIRESTORE
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("vault")
            .document(gameId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Game removed from Firestore: $gameId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing game", e)
                onFailure(e.message ?: "Unknown error")
            }
    }

    private fun Game.toLocalGame(): LocalGame {
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
    private fun LocalGame.toGame(): Game {
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