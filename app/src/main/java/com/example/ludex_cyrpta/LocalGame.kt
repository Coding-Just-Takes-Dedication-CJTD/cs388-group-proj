package com.example.ludex_cyrpta

import androidx.room.Entity
import androidx.room.PrimaryKey
//save user's games locally on device
@Entity(tableName = "saved_games")
data class LocalGame(
    @PrimaryKey val id: Int,
    val name: String,
    val rating: Double,
    val imageLink: String,
    val releaseDate: String,
    val descr: String,
    val synopsis: String,
    val trailerLink: String,
    val website: String,
    // Complex lists handled by Converters
    val genreTag: List<String>,
    val themeTag: List<String>,
    val gameModeTag: List<String>,
    val platformTag: List<String>,
    val otherServicesTag: List<String>,

    // Flags to know where this game belongs
    var inVault: Boolean = false,
    var inWishlist: Boolean = false
)