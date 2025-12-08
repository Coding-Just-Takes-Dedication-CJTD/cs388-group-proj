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
import java.net.URI

private const val TAG = "GameViewModel"

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameList = MutableLiveData<List<Game>>()
    private val _gameObj = MutableLiveData<Game?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errMsg = MutableLiveData<String?>()
    private val _currQuery = MutableLiveData<String?>()
    private val _currFilter = MutableLiveData<String?>(null)

    //frontend accessible versions of private MutableLiveData
    val gameList: LiveData<List<Game>> = _gameList
    val gameObj: LiveData<Game?> = _gameObj
    val isLoading: LiveData<Boolean> = _isLoading
    val errMsg: LiveData<String?> = _errMsg
    val currQuery: LiveData<String?> = _currQuery
    val currFilter: LiveData<String?> = _currFilter

    //make the RecView "infinite" by adding page buffers
    private var currPage = 0 //current page number
    private val pageBuffer = 100 //number of games per page
    var isLastPage = false //bool for if it reached the last page for "infinite" RecView

    //initiate the network client stuff
    private val client = OkHttpClient()
    private val tokenRepo = TwitchTokenRepo(application.applicationContext)
    private val authService = TwitchAuthService(tokenRepo, client)

    private val scope = viewModelScope

    private val gameDao = AppDatabase.getDatabase(application).gameDao()
    
    //private lateinit var trendingGamesList: MutableList<String>

    // no list generation here because multiple fragments use GameViewModel
    init {
        _isLoading.value = false
        _errMsg.value = null
    }

    fun loadMoreGames() {
        if (_isLoading.value == false && !isLastPage) {
            //only load more games when no search query or filter is active
            if (_currQuery.value.isNullOrBlank() && _currFilter.value.isNullOrBlank()){
                currPage++
                fetchGameListData("games")
            }  else {
                Log.w(TAG, "Cannot load more games while a search query or filter is active.")
            }
        }
    }

    //default Game List Generation (no filters or searches done)
    fun fetchGameListData(endpoint: String) = scope.launch {
        _isLoading.postValue(true)
        _errMsg.postValue(null)

        val offset = currPage * pageBuffer //calculate offset based on current page

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

        try {
            val query = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; sort name asc; limit $pageBuffer; offset $offset;"
            val respBody = makeRequest(accessToken, endpoint, query)
            val newGamesGen = parseResponse(respBody)

            val currList = _gameList.value.orEmpty().toMutableList()
            currList.addAll(newGamesGen)
            _gameList.postValue(currList)

            if (newGamesGen.size < pageBuffer) {
                isLastPage = true
                Log.i(TAG, "Fetch successful. Reached the last page (page ${currPage + 1})")
            } else {
                isLastPage = false
                Log.i(TAG, "Fetch Successful. Currently on page ${currPage + 1} and total games: ${_gameList.value?.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed: ${e.message}", e)
            _errMsg.postValue("Data Fetch Failed: ${e.message}")
            if (currPage > 0) currPage--
        } finally {
            _isLoading.postValue(false)
        }
    }

    // UPDATED: Offline-First Fetch Logic
    // We added 'gameID' as an optional parameter to check the local DB first
    fun fetchGameDetails(gameName: String, gameID: Int? = null) {
        scope.launch {
            _isLoading.postValue(true)
            _gameObj.postValue(null) // Clear previous data
            _errMsg.postValue(null)

            // 1. CHECK LOCAL STORAGE (ROOM) FIRST
            // If we have an ID, we check our backpack (Room DB)
            if (gameID != null) {
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
                _gameObj.postValue(null) //clear data if error occurs
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchTrendingGames(endpoint: String = "games") {
        scope.launch {
            _isLoading.postValue(true)
            _errMsg.postValue(null)
            _gameList.postValue(emptyList())

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

            try {
                val query = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; sort total_rating desc; where total_rating_count > 50; limit 20;"
                val respBody = makeRequest(accessToken, endpoint, query)
                val trendingGen = parseResponse(respBody)
                _gameList.postValue(trendingGen)
                
                //save to trendingGamesList
                

                Log.i(TAG, "Fetch of Trending Games was successful")
            } catch (e: Exception) {
                Log.e(TAG, "Fetch failed: ${e.message}", e)
                _errMsg.postValue("Data Fetch Failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchGames(query: String, filter: String?, endpoint: String = "games") {
        scope.launch {
            _isLoading.postValue(true)
            _errMsg.postValue(null)

            _currQuery.postValue(query)
            _currFilter.postValue(filter)

            val accessToken: String
            try {
                accessToken = authService.tokenCheck()
            } catch (e: Exception) {
                // If token fails, show error and stop loading
                _errMsg.postValue("Auth Failed: ${e.message}")
                _isLoading.postValue(false)
                return@launch
            }

            //original searchGames filter
            val safeQuery = query.replace("\"", "\\\"")
            var finalQuery = "fields id, name, total_rating, cover.image_id, first_release_date, storyline; search \"$safeQuery\";"

            //apply filter if it exists
            if (!filter.isNullOrBlank()) {
                val storeCat = if (filter == "steam") {
                    1
                } else if (filter == "epic") {
                    26
                } else if (filter == "ps") {
                    36
                } else if (filter == "xbox") {
                    31
                } else {
                    0
                }
                if (storeCat != 0) finalQuery += " where external_games.category = $storeCat;"
            }

            try {
                val respBody = makeRequest(accessToken, endpoint, finalQuery)
                val searchResults = parseResponse(respBody)

                _gameList.postValue(searchResults)
                isLastPage = true
                Log.i(TAG, "Fetch successful!")
            } catch (e: Exception) {
                Log.e(TAG, "Fetch failed: ${e.message}", e)
                _errMsg.postValue("Search Failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun applyFilter(filter: String?)  {
        val currentQuery = _currQuery.value
        _currFilter.value = filter //update the filter state

        if (!currentQuery.isNullOrBlank()) {
            //if a search query is active, re-run the search with the new filter
            searchGames(currentQuery, filter)
        } else if (!filter.isNullOrBlank()) {
            //if no search query but a filter is applied, perform a search on an empty string to apply the filter
            searchGames("", filter)
        }
        else {
            //if filter is null/cleared and there's no query, reset to default paginated list
            resetToDefault()
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
                    sanitizeURL(webARR?.optJSONObject(0)?.optString("url") ?: "")
                } else {
                    ""
                }
                
                //15. trending check


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

    //sanitize URL since we removed the "websites.trusted = true;" condition from the query (it grossly limited game result)
    private fun sanitizeURL(url: String): String {
        if (url.isEmpty() || url.isBlank()) return ""
        val trimmedURL = url.trim().filter { it >= ' ' }

        val cleanedByURI = try {
            URI(trimmedURL)
        } catch (err: Exception) {
            Log.e(TAG, "Malformed URL \"$url\" rejected", err)
            return ""
        }

        if (trimmedURL.startsWith("//")) {
            Log.w(TAG, "URL \"$url\" rejected due to protocol-relative nature")
            return ""
        }

        val scheme = cleanedByURI.scheme?.lowercase(Locale.ROOT)
        when (scheme) {
            "http", "https" -> {
                if (trimmedURL.lowercase(Locale.ROOT).startsWith("$scheme://")) return cleanedByURI.normalize().toString()
                else {
                    Log.w(TAG, "Rejected malformed smuggled scheme in \"$url\"")
                    return ""
                }
            }
            null -> { return "https://$trimmedURL" }
            else -> {
                Log.w(TAG, "Blocked unsafe URL scheme: $scheme within $url")
                return ""
            }
        }
    }

    //resets the search page back to its default if the search query and filters are clear/empty
    fun resetToDefault() {
        //reset page counter and infinite scrolling
        currPage = 0
        isLastPage = false

        //clear and reset the ViewModel states and then show the defaultList
        _currQuery.postValue(null)
        _currFilter.postValue(null)
        _gameList.postValue(emptyList())
        _gameList.postValue(emptyList())
        fetchGameListData("games")
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
            trending = this.trending,
            website = this.website
        )
    }
 }