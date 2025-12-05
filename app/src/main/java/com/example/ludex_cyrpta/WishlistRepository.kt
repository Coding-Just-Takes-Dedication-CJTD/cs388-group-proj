package com.example.ludex_cyrpta

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class WishlistRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "WishlistRepository"

    // 1. Add to Wishlist
    fun addGameToWishlist(game: Game, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
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

    // 2. Remove from Wishlist
    fun removeGameFromWishlist(gameId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("wishlist")
            .document(gameId.toString())
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error removing from wishlist") }
    }

    // 3. Check status
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

    // 4. Get all Wishlist games
    fun getWishlistGames(onResult: (List<Game>) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("No user logged in")
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
                        Log.e(TAG, "Error parsing game", e)
                    }
                }
                onResult(gameList)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error fetching wishlist") }
    }
}