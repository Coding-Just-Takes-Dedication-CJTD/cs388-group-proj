package com.example.ludex_cyrpta

class GameFetcher {
    companion object {
        val gameNames = listOf(
            "The Sims", "World of Warcraft", "GTA: San Andreas", "GTA V", "Halo 3",
            "Portal", "League of Legends", "Minecraft", "Red Dead Redemption", "Skyrim",
            "Legend of Zelda: Breath of the Wild", "PUBG", "Fortnite", "Fallout New Vegas", "Elden Ring"
        )
        val gameTags = listOf("PC", "RPG")
        val saleORtrend = listOf(true, false)

        fun getGames(): MutableList<Game> {
            val games: MutableList<Game> = ArrayList()

            // Use indices to safely loop through the names
            for (i in gameNames.indices) {
                // Logic for trending status
                val isTrending = if ((i + 1) % 2 == 0) saleORtrend[1] else saleORtrend[0]

                // Create the Game object using the NEW constructor
                // We use 'i' as the unique ID for Room
                val game = Game(
                    id = i,
                    name = gameNames[i],
                    rating = 4.5, // Default rating
                    imageLink = "", // Replace with real URL or resource string if needed
                    genreTag = gameTags,
                    themeTag = emptyList(),
                    gameModeTag = emptyList(),
                    platformTag = emptyList(),
                    otherServicesTag = emptyList(),
                    releaseDate = "2024-01-01", // Default date
                    trailerLink = "https://youtu.be/QdBZY2fkU-0",
                    descr = "Description for ${gameNames[i]}...",
                    synopsis = "A short synopsis of the game.",
                    listBelong = mapOf("GameVault" to (i % 5)),
                    trending = isTrending,
                    website = "https://www.google.com"
                )
                games.add(game)
            }
            return games
        }
    }
}