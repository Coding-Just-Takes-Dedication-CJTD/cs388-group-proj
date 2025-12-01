package com.example.ludex_cyrpta

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val TAG = "GameViewModel"

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameList = MutableLiveData<List<Game>>()
    private val _gameObj = MutableLiveData<Game?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errMsg = MutableLiveData<String?>()

    //frontend accessible versions of _gameList, _isLoading, and _errMsg
    val gameList: LiveData<List<Game>> = _gameList
    val gameObj: LiveData<Game?> = _gameObj
    val isLoading: LiveData<Boolean> = _isLoading
    val errMsg: LiveData<String?> = _errMsg

    //initiate the network client stuff
    private val client = OkHttpClient()
    private val tokenRepo = TwitchTokenRepo(application.applicationContext)
    private val authService = TwitchAuthService(tokenRepo, client)

    private val scope = viewModelScope

    init {
        _isLoading.value = false
        _errMsg.value = null
        fetchGameListData("games")
    }

    fun fetchGameListData(endpoint: String) = scope.launch {
        _isLoading.value = true
        _errMsg.value = null

        val accessToken: String
        try {
            accessToken = authService.tokenCheck()
            Log.i(TAG, "Token fetched successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Cannot fetch token. Error: ${e.message}", e)
            _errMsg.value = "Auth Failed: ${e.message}"
            _isLoading.value = false
            return@launch
        }

        try {
            val query = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; sort name asc; limit 50; where websites.trusted = true;"
            val respBody = makeRequest(accessToken, endpoint, query)
            val gameGen = parseResponse(respBody)
            _gameList.value = gameGen

            Log.i(TAG, "Fetch Successful")
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed: ${e.message}", e)
            _errMsg.value = "Data Fetch Failed: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun fetchGameDetails(gameName: String, endpoint: String = "games") {
        scope.launch {
            _isLoading.postValue(true)
            _gameObj.postValue(null) //to clear the previous object

            val accessToken: String
            try {
                accessToken = authService.tokenCheck()
                Log.i(TAG, "Token fetched successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Cannot fetch token. Error: ${e.message}", e)
                _errMsg.postValue("Auth Failed: ${e.message}")
                _isLoading.postValue(false)
                return@launch
            }

            val injectable = gameName.replace("\"", "\\\"")
            val query = "fields id, name, total_rating, cover.image_id, genres.name, themes.name, game_modes.name, platforms.abbreviation, external_games.external_game_source.name, first_release_date, videos.video_id, summary, storyline, websites.url; where name = \"$injectable\";"
            try {
                val respBody = makeRequest(accessToken, endpoint, query)
                val gameDetail = parseDetailedResponse(respBody)

                if (gameDetail == null) {
                    throw GameNotFoundException("No game details found for '$gameName'. Name might be misspelled or unavailable.")
                } else {
                    _gameObj.postValue(gameDetail)
                }

                Log.i(TAG, "Fetch Successful for $gameName")
            } catch (e: Exception) {
                Log.e(TAG, "Details fetch failed: ${e.message}", e)

                val errorMsg = if (e is GameNotFoundException) {
                    e.message
                } else {
                    "Details Data Fetch Failed: ${e.message}"
                }
                _errMsg.postValue(errorMsg)
                _gameObj.postValue(null) //clear data if error occurs
            } finally {
                _isLoading.postValue(false)
            }
        }

    }

    private fun getNames(jsonOBJ: JSONObject, fieldName: String, fieldType: String = "name"): List<String> {
        val fieldML: MutableList<String> = mutableListOf()
        val fieldARR = jsonOBJ.optJSONArray(fieldName) ?: return emptyList()
        for (i in 0 until fieldARR.length()) {
            val fieldELEM = fieldARR.optJSONObject(i) ?: continue //skip non-existent objects (shouldn't really matter lol
            fieldML.add(fieldELEM.optString(fieldType))
        }
        return fieldML.toList()
    }

    private fun parseResponse(jsonSTR: String): List<Game> {
        val finalGameList = mutableListOf<Game>()
        try {
            //pull fields from API (in reception order)
            val jsonARR = JSONArray(jsonSTR)
            for (i in 0 until jsonARR.length()) {
                val jsonOBJ = jsonARR.optJSONObject(i) ?: continue //skip object if null or other thing

                //get release date
                val debutDate = if (jsonOBJ.has("first_release_date")) {
                    val pulledDate = jsonOBJ.optLong("first_release_date", 0L) * 1000
                    if (pulledDate == 0L) { //NOTE: no games were released on the UTC Epoch, per IGDB
                        "n/a"
                    } else {
                        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date(pulledDate))
                    }
                } else {
                    "n/a"
                }

                //image generation
                val coverImageID = jsonOBJ.optJSONObject("cover")?.optString("image_id") ?: ""

                //get RecView-specific Game instance and add to GameList
                val gameOBJ = Game(
                    id = jsonOBJ.getInt("id"), //NOTE: potential crash point in IGDB API if any error; don't change to optInt unless need arises
                    name = jsonOBJ.optString("name", "Unknown Game"),
                    rating = String.format(Locale.getDefault(), "%.2f", jsonOBJ.optDouble("total_rating", 0.0)).toDouble(), //get rating and round to 2 decimal place
                    imageLink = if (coverImageID.isNotEmpty()) "https://images.igdb.com/igdb/image/upload/t_thumb/${coverImageID}.jpg" else "",
                    releaseDate = debutDate,
                    synopsis = jsonOBJ.optString("storyline", "...No storyline available"),
                )
                finalGameList.add(gameOBJ)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON response.", e)
        }
        return finalGameList
    }

    private fun parseDetailedResponse(jsonSTR: String): Game? {
        val gameOBJ: Game
        try {
            //pull fields from API (in reception order)
            val jsonARR = JSONArray(jsonSTR)

            if (jsonARR.length() == 1) {
                val jsonOBJ = jsonARR.getJSONObject(0)

                //1. id is pulled directly in Game instance

                //2. image generation
                val coverImageID = jsonOBJ.optJSONObject("cover")?.optString("image_id") ?: ""

                //3. otherServices generation
                val extGamesML: MutableList<String> = mutableListOf()
                val extGameARR = jsonOBJ.optJSONArray("external_games")
                extGameARR?.let {array ->
                    for (i in 0 until array.length()) {
                        val extGameOBJ = array.optJSONObject(i) ?: continue
                        val sourceName = extGameOBJ.optJSONObject("external_game_source")?.optString("name")
                        if (!sourceName.isNullOrEmpty()) {
                            extGamesML.add(sourceName)
                        }
                    }

                }
                val altServiceList = extGamesML.toList().ifEmpty { listOf("n/a") }

                //4. get release date
                val debutDate = if (jsonOBJ.has("first_release_date")) {
                    val pulledDate = jsonOBJ.optLong("first_release_date", 0L) * 1000
                    if (pulledDate == 0L) { //no games were released on the UTC Epoch, per IGDB
                        "n/a"
                    } else {
                        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date(pulledDate))
                    }
                } else {
                    "n/a"
                }

                //5. game mode generation
                val modesList = getNames(jsonOBJ, "game_modes").ifEmpty { listOf("n/a") }

                //6. genres generation
                val genresList = getNames(jsonOBJ, "genres").ifEmpty { listOf("n/a") }

                //7. name is pulled directly in Game instance

                //8. platform generation
                val platformList = getNames(jsonOBJ, "platforms", "abbreviation").ifEmpty { listOf("n/a") }

                //9. synopsis is pulled directly in Game instance

                //10. descr is pulled directly in Game instance

                //11. themes generation
                val themesList = getNames(jsonOBJ, "themes").ifEmpty { listOf("n/a") }

                //12. rating is pulled directly in Game instance

                //13. video generation
                val videoImageID = if (jsonOBJ.has("videos")) {
                    val videoARR = jsonOBJ.optJSONArray("videos")
                    videoARR?.optJSONObject(0)?.optString("video_id") ?: ""
                } else {
                    ""
                }

                //14. website link generation
                val webLink = if (jsonOBJ.has("websites")) {
                    val webARR = jsonOBJ.optJSONArray("websites")
                    webARR?.optJSONObject(0)?.optString("url") ?: ""
                } else {
                    ""
                }


                //get full Game instance to populate GameDetails page and to
                gameOBJ = Game(
                    id = jsonOBJ.getInt("id"),
                    name = jsonOBJ.optString("name", "Unknown Game"),
                    rating = String.format(Locale.getDefault(), "%.2f", jsonOBJ.optDouble("total_rating", 0.0)).toDouble(), //get rating and round to 1 decimal place,
                    imageLink = if (coverImageID.isNotEmpty()) "https://images.igdb.com/igdb/image/upload/t_thumb/${coverImageID}.jpg" else "",
                    genreTag = genresList,
                    themeTag = themesList,
                    gameModeTag = modesList,
                    platformTag = platformList,
                    otherServicesTag = altServiceList,
                    releaseDate = debutDate,
                    trailerLink = if (videoImageID.isNotEmpty()) "https://www.youtube.com/embed/${videoImageID}" else "",
                    descr = jsonOBJ.optString("summary", "...No description available"),
                    synopsis = jsonOBJ.optString("storyline", "...No storyline available"),
                    listBelong = emptyMap(),
                    trending = false,
                    website = webLink
                )
                return gameOBJ
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON response.", e)
            return null
        }
    }

    private suspend fun makeRequest(accessToken: String, endpoint: String, requestBodyText: String): String {
        return withContext(Dispatchers.IO) {
            val mediaType = "text/plain; charset=utf-8".toMediaType()
            val reqBody = requestBodyText.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://api.igdb.com/v4/$endpoint")
                .header("Client-ID", "yhd1x8r5xwwjreivnsg7p3epkwfqy2")
                .header("Authorization", "Bearer $accessToken")
                .post(reqBody)
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string()

                if (response.isSuccessful) {
                    return@withContext respBody ?: throw Exception("Empty Data Response Body")
                } else {
                    throw Exception("IGDB Request Failed!\nCode: ${response.code}\n$respBody")
                }
            }
        }
    }
 }