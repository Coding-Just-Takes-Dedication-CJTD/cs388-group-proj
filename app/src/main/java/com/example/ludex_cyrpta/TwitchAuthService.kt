package com.example.ludex_cyrpta

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val TAG = "TwitchAuthService"

class TwitchAuthService(
    private val tokenRepo: TwitchTokenRepo,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun tokenCheck(): String {
        val existingToken = tokenRepo.getValidToken()
        if (existingToken != null) {
            Log.i(TAG, "Token is valid and retrieved from repo.")
            return existingToken
        } else {
            Log.d(TAG, "No valid token found. Initiating network request...")
            return fetchToken()
        }
    }

    private suspend fun fetchToken(): String {
        Log.d(TAG, "Attempting token fetch...")

        val respBody = makeRequest() //body of the response of the POST request

        //parse & save the token's data
        val json = JSONObject(respBody)
        val accessToken = json.getString("access_token")
        val expiration = json.getLong("expires_in")

        tokenRepo.saveToken(accessToken, expiration)
        Log.i(TAG, "Token saved successfully.")
        return accessToken
    }

    private suspend fun makeRequest(): String {
        return withContext(Dispatchers.IO) {
            val reqBody = FormBody.Builder() //body of request
                .add("client_id", "yhd1x8r5xwwjreivnsg7p3epkwfqy2")
                .add("client_secret", "65q85rte56hrhv3suyjuhff4hyd794")
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url("https://id.twitch.tv/oauth2/token")
                .post(reqBody)
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string()

                if (response.isSuccessful) {
                    return@withContext respBody ?: throw Exception("Empty Response Body")
                } else {
                    throw IOException("Token Request Failed!\nCode: ${response.code}\nBody: ${respBody}")
                }
            }
        }
    }
}