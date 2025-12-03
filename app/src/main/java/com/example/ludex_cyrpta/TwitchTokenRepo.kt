package com.example.ludex_cyrpta

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TwitchTokenRepo(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("twitch_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "replace_token"
        private const val KEY_EXPIRY_TIME = "token_time"
        private const val EXPIRY_LEEWAY_MS = 60000L //1 minute buffer to give time to get new token
        private const val TAG = "TwitchTokenRepo"
    }

    fun saveToken(
        accessToken: String, //the name of the access token (the requested returned message of the POST OAuth request)
        expirationSec: Long //lifespan of the token in seconds (the other returned message to the POST OAuth request)
    ) {
        val expirationMS = System.currentTimeMillis() + (expirationSec * 1000)
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putLong(KEY_EXPIRY_TIME, expirationMS)
            apply()
        }
        Log.i(TAG, "Token saved. Expires at: $expirationMS") //confirmation it saved
    }

    fun getValidToken() : String? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiration = prefs.getLong(KEY_EXPIRY_TIME, 0L)
        val timeRightNow = System.currentTimeMillis()

        //check if there isn't a valid access token
        if ((accessToken == null) || (accessToken == "replace_token")) {
            Log.d(TAG, "...No token found")
            return null
        }

        //check if the token is still within the token valid time range
        if (timeRightNow >= expiration - EXPIRY_LEEWAY_MS) {
            Log.w(TAG, "Token has or is nearly expired")
            return null
        }

        Log.d(TAG, "Returning valid token...")
        return accessToken
    }
}