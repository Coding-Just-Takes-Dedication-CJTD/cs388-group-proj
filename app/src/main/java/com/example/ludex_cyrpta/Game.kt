package com.example.ludex_cyrpta

data class Game(
    val id: Int, //unique id pulled from API request; tied to API Game field "id"
    val name: String, // ✅ game name; i.e. "Doom"; tied to API Game field "name"
    val rating: Double = 0.0, // ✅ total game rating; tied to API Game field "total_rating";
    val imageLink: String, //link to game poster; tied to API Game field "cover.image_id"
    val genreTag: List<String>, // ✅ game's genres; tied to API Game field "genres.name"
    val themeTag: List<String>, //game's themes; tied to API Game field "themes.name"
    val gameModeTag: List<String>, //game mode; tied to API Game field "game_modes.name"
    val platformTag: List<String>, //game platform; tied to API Game field "platforms.abbreviation"
    val otherServicesTag: List<String>, //other services the game appears on; tied to API Game field "external_games.external_game_source.name"
    val releaseDate: String, // ✅ the date the game first released; tied to API field "first_release_date"
    val trailerLink: String, //link to trailer; i.e. https://youtu.be/QdBZY2fkU-0 (GTA 6 Trailer 1); tied to API Game field "videos.video_id"
    val descr: String, //game description; tied to API Game field "summary"
    val synopsis: String, //game synopsis; tied to API Game field "storyline"
    val listBelong: Map<String, Int>, //listBelong -> list game belongs to; key is list name (GameVault, WishList, Favorites, if connected {Steam, Epic, XBOX, Playstation} and value is position on the list (1, 2, 3, ...)
    val trending: Boolean, //is the game trending? TRUE or FALSE
    val website: String, //website of the game; tied to API Game field "websites.url"
) {
    // search query-specific constructor
    constructor(id: Int, name: String, rating: Double, imageLink: String, releaseDate: String, synopsis: String):
        this(
            id,
            name,
            rating,
            imageLink,
            emptyList<String>(),
            emptyList<String>(),
            emptyList<String>(),
            emptyList<String>(),
            emptyList<String>(),
            releaseDate,
            "",
            "",
            synopsis,
            emptyMap<String, Int>(),
            false,
            ""
        ) {}
}

/* Game API Fields We Might Need (in parameter order):
    ✅ -- id -> unique game ID

    ✅ -- name -> name of game; type: String

    ✅ -- total_rating -> rating from critic scores; type: Double

    ✅ -- cover -> the cover of the game; type: reference Cover ID (Int)
        API endpoint "https://api.igdb.com/v4/covers" fields:
            image_id --> image ID used to construct image link (contingent on usefulness); type: String
        <<TODO: "cover.image_id" returns both id & image so grab only the image_id and put into this template>>
        <<TODO: IGDB image template -- https://images.igdb.com/igdb/image/upload/t_{size_template}/{image_id}.jpg>>
            <<TODO: size_template codes to choose are either "t_thumb" for tiny thumbnails (90x128), "t_cover_small" for small cover (90x128), or "t_cover_big" for big cover (264x374)>>

    ✅ -- genres -> genres of the game; type: array of Genre IDs (Int)
        API endpoint "https://api.igdb.com/v4/genres" fields:
            name --> name of the genre; type: String
        <<TODO: "genres.name" returns both id & name so extract the "name" and put it into a List<name (String)>>>
        <<TODO: filterQuery = "<searchesQuery>; where genres.name = "<filter>";">>
            choose from: Adventure, Arcade, Card & Board Game, Fighting, Indie, MOBA, Music, Platform, Point-and-click, Puzzle, Quiz/Trivia, Racing, Real Time Strategy (RTS), Role-playing (RPG), Shooter, Simulator, Sport, Strategy, Turn-based strategy (TBS), Visual Novel

    ✅ -- themes -> game themes; type: array of Theme IDs
        API endpoint "https://api.igdb.com/v4/themes" fields:
            name --> name of the theme; type: Strings
       <<TODO: "themes.name" returns both id & name so extract the "name" and put it into a List<name (String)>>>
       <<TODO: filterQuery = "<searchesQuery>; where themes.name = "<filter>";">>
           choose from: Action, Business, Comedy, Drama, Educational, Fantasy, Historical, Horror, Mystery, Non-fiction, Open world, Romance, Sandbox, Science fiction, Stealth, Survival, Thriller

    ✅ -- game_modes -> modes of gameplay (singleplayer and multiplayer; needed for filter); type: array of Game Mode IDs (Int)
        API endpoint "https://api.igdb.com/v4/game_modes" fields:
            name --> name of the game mode
       <<TODO: "game_modes.name" returns both id & name so extract the "name" and put it into a List<name (String)>>>
       <<TODO: filterQuery = "<searchesQuery>; where game_modes.name = "<filter>";">>
           choose from: Battle Royale, Co-operative, Massively Multiplayer Online (MMO), Multiplayer, Single player, Split screen

    ✅ -- platforms -> platforms game was released on; type: array of Platform IDs
        API endpoint "https://api.igdb.com/v4/platforms" fields:
            abbreviation --> platform name abbreviation; type: String
       <<TODO: "platforms.abbreviation" returns both id & abbreviation so extract the "abbreviation" and put it into a List<abbrev (String)>>>
       <<TODO: either here or in adapter, do name case corrections, abbrev = "Browser" & optString(abbrev, "n/a")>>
       <<TODO: filterQuery = "<searchesQuery>; where platforms.abbreviation = "<filter>";">>
            choose from: browser, Game Boy, Linux, Mac, PC, PS1, PS2, PS3, PS4, PSP, Wii, WiiU, X360, XBOX, XONE

    ✅ -- external_games -> external IDs this game has on other services (contingent on usefulness); type: array of External Game IDs (Int)
        API endpoint "https://api.igdb.com/v4/external_games" fields:
            external_game_source --> source of external game; type: reference ID for External Game Source (https://api.igdb.com/v4/external_game_sources)
       <<TODO: "external_games.external_game_source.name" returns both id & name so extract the "name" and put it into a List<name (String)>>>
        <<TODO: filterQuery = "<searchesQuery>; where external_games.external_game_source.name = "<filter>";">>
            choose from: Steam, Playstation Store US, Epic Games Store, Xbox Marketplace

    ✅ -- first_release_date -> first release date of the game; type: Unix Time Stamp

    ✅ -- videos -> videos of the game (youtube embed links); type: array of Game Video IDs (Int)
        API endpoint "https://api.igdb.com/v4/game_videos" fields:
            video_id --> external ID of the Youtube video; type: String
        <<TODO: "videos.video_id" returns both id & image so grab only the first video_id and put into this template>>
        <<TODO: IGDB YouTube embed template -- https://www.youtube.com/embed/{video_id}>>
        <<TODO: IGDB YouTube thumbnail template -- https://img.youtube.com/vi/{video_id}/maxresdefault.jpg>>
            to render this, do trailerLink.substrtingAfterLast("/") into the Glide or whatever

    ✅ -- summary -> game description; type: String

    ✅ -- storyline -> game storyline; type: String

    ✅ -- websites -> websites associated with the game; type: array of Website IDs (Int)
        API endpoint "https://api.igdb.com/v4/websites" fields:
            url --> URL of website; type: String
        <<TODO: "websites.url" returns both id & url so grab only the first url>>
 */