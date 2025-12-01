package com.example.ludex_cyrpta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameDao {
    // Saves game. If ID exists, it overwrites (updates) it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)

    // Get all saved games
    @Query("SELECT * FROM games_table")
    suspend fun getAllGames(): List<Game>
}