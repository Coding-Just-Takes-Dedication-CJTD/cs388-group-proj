package com.example.ludex_cyrpta

import android.content.Context

class SteamPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("steam_prefs", Context.MODE_PRIVATE)

    fun getSteamId(): String? {
        return prefs.getString("steam_id", null)
    }

    fun setSteamId(id: String?) {
        prefs.edit().putString("steam_id", id).apply()
    }

    fun isLinked(): Boolean {
        return getSteamId() != null
    }
}
