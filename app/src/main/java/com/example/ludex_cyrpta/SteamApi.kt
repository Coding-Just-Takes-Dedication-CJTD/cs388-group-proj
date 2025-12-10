package com.example.ludex_cyrpta

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SteamApi {

    private const val API_KEY = "B462116379A38A5BCE1F220B6910CB31"

    suspend fun getSteamProfile(steamId: String): SteamProfile? {
        return withContext(Dispatchers.IO) {
            try {
                val urlStr =
                    "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/" +
                            "?key=$API_KEY&steamids=$steamId"

                val conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val players = json.getJSONObject("response").getJSONArray("players")

                if (players.length() == 0) return@withContext null

                val p = players.getJSONObject(0)

                SteamProfile(
                    steamId = p.getString("steamid"),
                    name = p.optString("personaname", "Unknown User"),
                    avatar = p.optString("avatarfull", "")
                )

            } catch (e: Exception) {
                Log.e("SteamApi", "Error: ${e.message}")
                null
            }
        }
    }
}

data class SteamProfile(
    val steamId: String,
    val name: String,
    val avatar: String
)