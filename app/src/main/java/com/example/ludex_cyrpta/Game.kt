package com.example.ludex_cyrpta

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// 1. Add Entity annotation
// 2. Add PrimaryKey annotation to 'id'
@Entity(tableName = "games_table")
data class Game(
    @PrimaryKey val id: Int,
    val name: String,
    val rating: Double = 0.0,
    val imageLink: String,
    val genreTag: List<String> = emptyList(),
    val themeTag: List<String> = emptyList(),
    val gameModeTag: List<String> = emptyList(),
    val platformTag: List<String> = emptyList(),
    val otherServicesTag: List<String> = emptyList(),
    val releaseDate: String,
    val trailerLink: String,
    val descr: String,
    val synopsis: String,
    val listBelong: Map<String, Int> = emptyMap(),
    val trending: Boolean,
    val website: String
): Serializable {
    // Existing toMap function stays exactly the same
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "rating" to rating,
            "imageLink" to imageLink,
            "genreTag" to genreTag,
            "themeTag" to themeTag,
            "gameModeTag" to gameModeTag,
            "platformTag" to platformTag,
            "otherServicesTag" to otherServicesTag,
            "releaseDate" to releaseDate,
            "trailerLink" to trailerLink,
            "descr" to descr,
            "synopsis" to synopsis,
            "listBelong" to listBelong,
            "trending" to trending,
            "website" to website
        )
    }
}