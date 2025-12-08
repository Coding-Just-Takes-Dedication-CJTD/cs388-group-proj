package com.example.ludex_cyrpta

import androidx.room.*

@Dao
interface GameDao {
    // Save or Update a game
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: LocalGame)

    // Get specific game (Game Details Page)
    @Query("SELECT * FROM saved_games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): LocalGame?

    // Get all Vault games (Vault Page)
    @Query("SELECT * FROM saved_games WHERE inVault = 1")
    suspend fun getVaultGames(): List<LocalGame>

    // Get all Wishlist games (Wishlist Page)
    @Query("SELECT * FROM saved_games WHERE inWishlist = 1")
    suspend fun getWishlistGames(): List<LocalGame>
}