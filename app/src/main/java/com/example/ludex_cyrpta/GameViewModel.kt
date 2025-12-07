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

    //make the RecView "infinite" by adding page buffers
    private var currPage = 0 //current page number
    private val pageBuffer = 100 //number of games per page
    private var isLastPage = false //bool for if it reached the last page for "infinite" RecView

    //initiate the network client stuff
    private val client = OkHttpClient()
    private val tokenRepo = TwitchTokenRepo(application.applicationContext)
    private val authService = TwitchAuthService(tokenRepo, client)

    private val scope = viewModelScope

    private val gameDao = AppDatabase.getDatabase(application).gameDao()

    init {
        _isLoading.value = false
        _errMsg.value = null
        fetchGameListData("games")
    }

    fun loadMoreGames() {
        if (_isLoading.value == false && !isLastPage) {
            currPage++
            fetchGameListData("games")
        }
    }

    fun fetchGameListData(endpoint: String) = scope.launch {
        _isLoading.value = true
        _errMsg.value = null

        val offset = currPage * pageBuffer //calculate offset based on current page

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
            val query = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; sort name asc; limit $pageBuffer; offset $offset; where websites.trusted = true;"
            val respBody = makeRequest(accessToken, endpoint, query)
            val newGamesGen = parseResponse(respBody)

            val currList = _gameList.value.orEmpty().toMutableList()
            currList.addAll(newGamesGen)
            _gameList.value = currList

            if (newGamesGen.size < pageBuffer) {
                isLastPage = true
                Log.i(TAG, "Fetch successful. Reached the last page (page ${currPage + 1})")
            } else {
                isLastPage = false
                Log.i(TAG, "Fetch Successful. Currently on page ${currPage + 1} and total games: ${_gameList.value?.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed: ${e.message}", e)
            _errMsg.value = "Data Fetch Failed: ${e.message}"
            if (currPage > 0) currPage--
        } finally {
            _isLoading.value = false
        }
    }

    // UPDATED: Offline-First Fetch Logic
    // We added 'gameId' as an optional parameter to check the local DB first
    fun fetchGameDetails(gameName: String, gameId: Int? = null) {
        scope.launch {
            _isLoading.postValue(true)
            _gameObj.postValue(null) // Clear previous data
            _errMsg.postValue(null)

            // 1. CHECK LOCAL STORAGE (ROOM) FIRST
            // If we have an ID, we check our backpack (Room DB)
            if (gameId != null) {
                val localGame = gameDao.getGameById(gameId)

                if (localGame != null) {
                    Log.i(TAG, "Found game in local storage! Loading instantly.")
                    // Convert the local data back to a Game object and show it
                    _gameObj.postValue(localGame.toGame())

                    // Stop loading and EXIT. We don't need the internet.
                    _isLoading.postValue(false)
                    return@launch
                }
            }

            // 2. NOT FOUND LOCALLY? FETCH FROM INTERNET (IGDB API)
            Log.i(TAG, "Game not in local storage. Fetching from IGDB API...")

            val accessToken: String
            try {
                accessToken = authService.tokenCheck()
            } catch (e: Exception) {
                _errMsg.postValue("Auth Failed: ${e.message}")
                _isLoading.postValue(false)
                return@launch
            }

            val injectable = gameName.replace("\"", "\\\"")
            val query = "fields id, name, total_rating, cover.image_id, genres.name, themes.name, game_modes.name, platforms.abbreviation, external_games.external_game_source.name, first_release_date, videos.video_id, summary, storyline, websites.url; where name = \"$injectable\";"

            try {
                val respBody = makeRequest(accessToken, "games", query)
                val gameDetail = parseDetailedResponse(respBody)

                if (gameDetail == null) {
                    throw GameNotFoundException("No game details found for '$gameName'.")
                } else {
                    _gameObj.postValue(gameDetail)
                }
                Log.i(TAG, "Fetch Successful for $gameName")
            } catch (e: Exception) {
                Log.e(TAG, "Details fetch failed: ${e.message}", e)
                val errorMsg = if (e is GameNotFoundException) e.message else "Details Data Fetch Failed: ${e.message}"
                _errMsg.postValue(errorMsg)
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
    // Function to search for games based on user text
    fun searchGames(query: String) {
        // Launch a coroutine (background thread) so we don't freeze the app UI
        scope.launch {
            // value = true triggers the loading spinner in the UI
            _isLoading.value = true
            // Clear any old error messages so they don't show up on a new search
            _errMsg.value = null

            val accessToken: String
            try {
                // Check if we have a valid Twitch token; if not, fetch a new one
                accessToken = authService.tokenCheck()
            } catch (e: Exception) {
                // If token fails, show error and stop loading
                _errMsg.value = "Auth Failed: ${e.message}"
                _isLoading.value = false
                return@launch
            }

            // Sanitize input: Remove quotation marks from user input
            // This prevents the API query from breaking if the user types a " symbol
            val safeQuery = query.replace("\"", "")

            // Construct the IGDB API query.
            // 'search' keyword tells IGDB to look for matching names.
            // We request the same fields (id, name, rating, etc.) so the cards look the same.
            val apiQuery = "search \"$safeQuery\"; fields id, name, total_rating, cover.image_id, first_release_date, storyline; limit 50;"

            try {
                // Send the request to the internet
                val respBody = makeRequest(accessToken, "games", apiQuery)
                // Convert the raw JSON text into a list of Game objects
                val searchResults = parseResponse(respBody)

                // IMPORTANT: We overwrite the current list completely.
                // In the normal feed, we used .addAll(), but for search, we want ONLY these results.
                _gameList.value = searchResults

                // We set this to true to stop the "Infinite Scroll" logic.
                // Search results are usually a single batch, we don't want the app trying to load "Page 2" of a search.
                isLastPage = true
            } catch (e: Exception) {
                // If the network request fails, show the error message
                _errMsg.value = "Search Failed: ${e.message}"
            } finally {
                // Hide the loading spinner regardless of success or failure
                _isLoading.value = false
            }
        }
    }

    // Function to return the screen to normal when the user clears the search bar
    fun resetToDefaultList() {
        // Reset page counter to 0 so we start fresh
        currPage = 0
        // Re-enable infinite scrolling
        isLastPage = false
        // Clear the screen temporarily
        _gameList.value = emptyList()
        // Call the original function to load the standard "popular games" list
        fetchGameListData("games")
    }
    fun fetchTrendingGames() {
        scope.launch {
            _isLoading.value = true
            _gameList.value = emptyList() // Clear old data

            val accessToken: String
            try {
                accessToken = authService.tokenCheck()
            } catch (e: Exception) {
                _errMsg.value = "Auth Failed: ${e.message}"
                _isLoading.value = false
                return@launch
            }



            // sort total_rating desc: Put the highest scores (99, 98, etc.) at the top.
            // where total_rating_count > 30: filters out obscure games that have one single 100/100 review, only want games with enough votes to matter
            // want a top 50, so limit 50 results at the top when scrolling through
            val query = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; sort total_rating desc; where total_rating_count > 30; limit 50;"
            try {
                val respBody = makeRequest(accessToken, "games", query)
                val games = parseResponse(respBody)

                // overwrite the list
                _gameList.value = games
            } catch (e: Exception) {
                _errMsg.value = "Trending Fetch Failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun LocalGame.toGame(): Game {
        return Game(
            id = this.id,
            name = this.name,
            rating = this.rating,
            imageLink = this.imageLink,
            genreTag = this.genreTag,
            themeTag = this.themeTag,
            gameModeTag = this.gameModeTag,
            platformTag = this.platformTag,
            otherServicesTag = this.otherServicesTag,
            releaseDate = this.releaseDate,
            trailerLink = this.trailerLink,
            descr = this.descr,
            synopsis = this.synopsis,
            listBelong = emptyMap(),
            trending = false,
            website = this.website
        )
    }
 }