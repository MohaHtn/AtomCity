/**
 * Taiko View Model
 *
 * ViewModel managing the UI state, authentication, settings, and gameplay statistics for Taiko no Tatsujin features.
 */
package org.arcade.atomcity.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.UserPreferencesManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.ui.game.taiko.getTaikoGenreInfo
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.compose.resources.ExperimentalResourceApi
import atomcity.shared.generated.resources.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.util.decodeBase64String
import org.arcade.atomcity.data.remote.TaikoUser
import org.arcade.atomcity.data.remote.model.taikoserver.dan.DanCourseData
import org.arcade.atomcity.data.remote.model.taikoserver.dan.TaikoServerDanBestDataResponse
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.ui.game.taiko.stats.TaikoMostPlayedEntry
import org.arcade.atomcity.ui.game.taiko.stats.TaikoOverallProgressStats
import org.arcade.atomcity.ui.game.taiko.stats.TaikoProgressStats

class TaikoViewModel(
    private val usecase: GetTaikoServerDataUseCase,
    private val apiKeyManager: ApiKeyManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val scorefetcherRepository: IScorefetcherRepository
) : ViewModel() {

    // StateFlow to hold the music details data
    private val _scoresData = MutableStateFlow<TaikoServerPlayHistoryResponse?>(null)
    val scoresData = _scoresData

    // StateFlow to hold the music details data
    private val _musicDetailsData = MutableStateFlow<TaikoServerMusicDetailsResponse?>(null)
    val musicDetailsData = _musicDetailsData

    // StateFlow to hold the user settings data
    private val _userSettingsData = MutableStateFlow<TaikoServerUserSettingsResponse?>(null)
    val userSettingsData = _userSettingsData

    // StateFlow to hold the dashboard data
    private val _dashboardData = MutableStateFlow<String?>(null)
    val dashboardData = _dashboardData

    private val _userDetailedSettings = MutableStateFlow<TaikoServerUserSettingsResponse?>(null)
    val userDetailedSettings = _userDetailedSettings

    private val json = Json { ignoreUnknownKeys = true }

    private val _costumesData = MutableStateFlow<TaikoServerCostumesResponse?>(null)
    val costumesData = _costumesData

    private val _titlesData = MutableStateFlow<TaikoServerTitlesResponse?>(null)
    val titlesData = _titlesData

    private val _imagesData = MutableStateFlow<TaikoImagesData?>(null)
    val imagesData = _imagesData

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites = _showOnlyFavorites

    val favoriteSongIds: StateFlow<Set<Int>> = scoresData.map { scores ->
        scores?.songHistoryData?.filter { it.isFavorite == true }?.mapNotNull { it.songId }?.toSet() ?: emptySet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun onToggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launch {
            val currentScores = _scoresData.value
            val currentEntry = currentScores?.songHistoryData?.firstOrNull { it.songId == songId }
            val isCurrentlyFavorite = currentEntry?.isFavorite == true
            val newFavoriteState = !isCurrentlyFavorite

            if (currentScores != null) {
                val updatedHistory = currentScores.songHistoryData.map { entry ->
                    if (entry.songId == songId) {
                        entry.copy(isFavorite = newFavoriteState)
                    } else {
                        entry
                    }
                }
                _scoresData.value = currentScores.copy(songHistoryData = updatedHistory)
            }

            val baid = loggedInBaid
            if (baid != null) {
                try {
                    val token = apiKeyManager.getTaikoAuthToken()
                    usecase.postFavoriteSong(
                        request = TaikoFavoriteSongRequest(
                            baid = baid,
                            songId = songId,
                            isFavorite = newFavoriteState
                        ),
                        authToken = token
                    )
                    PlatformUtils.log("TaikoViewModel", "Posted favorite song: baid=$baid, songId=$songId, isFavorite=$newFavoriteState")
                } catch (e: Exception) {
                    PlatformUtils.log("TaikoViewModel", "Error posting favorite song: ${e.message}", true)
                    if (currentScores != null) {
                        _scoresData.value = currentScores
                    }
                }
            }
        }
    }

    val filteredScores: StateFlow<List<TaikoServerHistoryEntry>> = combine(scoresData, _searchQuery, _showOnlyFavorites) { scores, query, onlyFavs ->
        val list = if (query.isBlank()) {
            scores?.songHistoryData ?: emptyList()
        } else {
            scores?.songHistoryData?.filter { score ->
                score.musicName?.contains(query, ignoreCase = true) == true ||
                score.musicNameEN?.contains(query, ignoreCase = true) == true ||
                score.musicNameCN?.contains(query, ignoreCase = true) == true ||
                score.musicNameKO?.contains(query, ignoreCase = true) == true ||
                score.musicArtist?.contains(query, ignoreCase = true) == true ||
                score.musicArtistEN?.contains(query, ignoreCase = true) == true ||
                score.musicArtistCN?.contains(query, ignoreCase = true) == true ||
                score.musicArtistKO?.contains(query, ignoreCase = true) == true ||
                getTaikoGenreInfo(score.genre)?.name?.contains(query, ignoreCase = true) == true
            } ?: emptyList()
        }

        if (onlyFavs) {
            list.filter { it.isFavorite == true }
        } else {
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showDashboardTrigger = MutableStateFlow(false)
    val showDashboardTrigger = _showDashboardTrigger

    val showDashboardPreference = userPreferencesManager.showTaikoDashboard

    val isLoading = MutableStateFlow(_scoresData.value == null)
    val isRefreshing = MutableStateFlow(false)
    val isLoadingMusicDetails = MutableStateFlow(false)
    val isLoadingUserSettings = MutableStateFlow(false)
    val isLoadingScores = MutableStateFlow(false)

    internal val _currentPage = MutableStateFlow(1)

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private var currentBaid: Int? = null
    val loggedInBaid: Int? get() = currentBaid ?: _userSettingsData.value?.baid

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    fun setShowDashboardPreference(show: Boolean) {
        viewModelScope.launch {
            userPreferencesManager.setShowTaikoDashboard(show)
        }
    }

    fun dismissDashboard() {
        _showDashboardTrigger.value = false
    }

    private val _mostPlayedCharts = MutableStateFlow<List<TaikoMostPlayedEntry>>(emptyList())
    val mostPlayedCharts: StateFlow<List<TaikoMostPlayedEntry>> = _mostPlayedCharts

    private val _bestScores = MutableStateFlow<List<TaikoServerHistoryEntry>>(emptyList())
    val bestScores: StateFlow<List<TaikoServerHistoryEntry>> = _bestScores

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats

    private val _danCourses = MutableStateFlow<List<DanCourseData>>(emptyList())
    val danCourses: StateFlow<List<DanCourseData>> = _danCourses

    private val _userDanBestData = MutableStateFlow<TaikoServerDanBestDataResponse?>(null)
    val userDanBestData: StateFlow<TaikoServerDanBestDataResponse?> = _userDanBestData

    private val _isLoadingDan = MutableStateFlow(false)
    val isLoadingDan: StateFlow<Boolean> = _isLoadingDan

    fun fetchDanData() {
        viewModelScope.launch {
            _isLoadingDan.value = true
            try {
                if (_musicDetailsData.value == null) {
                    fetchMusicDetails()
                }
                usecase.getDanDataFlow().collect { courses ->
                    _danCourses.value = courses
                }
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Error fetching Dan data: ${e.message}")
            } finally {
                _isLoadingDan.value = false
            }
        }

        val baid = currentBaid
        if (baid != null) {
            viewModelScope.launch {
                try {
                    usecase.getDanBestDataFlow(baid.toString()).collect { bestData ->
                        _userDanBestData.value = bestData
                    }
                } catch (e: Exception) {
                    PlatformUtils.log("TaikoViewModel", "Error fetching Dan best data: ${e.message}")
                }
            }
        }
    }

    fun fetchBestScores() {
        viewModelScope.launch {
            _isLoadingStats.value = true
            val history = _scoresData.value?.songHistoryData ?: emptyList()
            val best = history.sortedByDescending { it.score }.take(30)
            _bestScores.value = best
            _isLoadingStats.value = false
        }
    }

    fun fetchMostPlayedCharts(
        isGlobal: Boolean,
        period: String = "alltime",
        date: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingStats.value = true
            
            if (_musicDetailsData.value == null) {
                try {
                    fetchMusicDetails()
                } catch (e: Exception) {
                    PlatformUtils.log("TaikoViewModel", "Error fetching music details in most played: ${e.message}")
                }
            }

            val myName = _userDetailedSettings.value?.myDonName?.takeIf { it.isNotBlank() } ?: "Moi"

            // Get all community entries with user names
            val communityEntries: List<Pair<String, TaikoServerHistoryEntry>> = if (isGlobal) {
                _communityScores.value.flatMap { (baid, historyResponse) ->
                    val user = _taikoUsers.value.find { it.baid == baid }
                    val userLabel = if (baid == currentBaid) {
                        myName
                    } else {
                        user?.nickname?.takeIf { it.isNotBlank() } ?: "Joueur $baid"
                    }
                    historyResponse.songHistoryData.map { entry -> userLabel to entry }
                }
            } else {
                (_scoresData.value?.songHistoryData ?: emptyList()).map { entry -> myName to entry }
            }

            // Ensure current user's local history is included if global and current user is not in communityScores
            val currentUserHistory = if (isGlobal && (currentBaid == null || !_communityScores.value.containsKey(currentBaid))) {
                (_scoresData.value?.songHistoryData ?: emptyList()).map { entry -> myName to entry }
            } else {
                emptyList()
            }

            val allHistoryWithUser = communityEntries + currentUserHistory

            val filteredEntries = if (period != "alltime" && !date.isNullOrBlank()) {
                allHistoryWithUser.filter { (_, entry) ->
                    val playTime = entry.playTime ?: return@filter false
                    when (period) {
                        "day" -> playTime.startsWith(date)
                        "month" -> playTime.startsWith(date)
                        "week" -> {
                            try {
                                val playDateStr = playTime.take(10)
                                val playDate = LocalDate.parse(playDateStr)
                                val startOfWeek = if (date.length == 10) {
                                    LocalDate.parse(date)
                                } else {
                                    val parts = date.split("-")
                                    if (parts.size == 2) {
                                        val year = parts[0].toInt()
                                        val week = parts[1].toInt()
                                        val jan4 = LocalDate(year, 1, 4)
                                        val dayOfWeekJan4 = jan4.dayOfWeek.isoDayNumber
                                        val firstMonday = jan4.minus(DatePeriod(days = dayOfWeekJan4 - 1))
                                        firstMonday.plus(DatePeriod(days = (week - 1) * 7))
                                    } else null
                                }
                                if (startOfWeek != null) {
                                    val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                                    playDate in startOfWeek..endOfWeek
                                } else {
                                    playTime.startsWith(date) || playTime.contains(date)
                                }
                            } catch (e: Exception) {
                                false
                            }
                        }
                        else -> true
                    }
                }
            } else {
                allHistoryWithUser
            }

            val grouped = filteredEntries.groupBy { (_, entry) ->
                (entry.songId ?: 0) to (entry.difficulty ?: 0)
            }
            
            val mostPlayed = grouped.map { (key, plays) ->
                val (songId, difficulty) = key
                val musicDetail = musicDetailsData.value?.get(songId.toString())
                val firstPlayWithName = plays.map { it.second }.find { !it.musicName.isNullOrBlank() }
                
                val musicName = firstPlayWithName?.musicName?.takeIf { it.isNotBlank() }
                    ?: musicDetail?.songName?.takeIf { it.isNotBlank() }
                    ?: "Morceau #${songId}"
                
                val musicNameEN = firstPlayWithName?.musicNameEN?.takeIf { it.isNotBlank() }
                    ?: musicDetail?.songNameEN?.takeIf { it.isNotBlank() }
                
                val musicArtist = firstPlayWithName?.musicArtist?.takeIf { it.isNotBlank() }
                    ?: musicDetail?.artistName?.takeIf { it.isNotBlank() }
                    ?: ""
                
                val musicArtistEN = firstPlayWithName?.musicArtistEN?.takeIf { it.isNotBlank() }
                    ?: musicDetail?.artistNameEN?.takeIf { it.isNotBlank() }
                
                val uniquePlayers = plays.map { it.first }.distinct().size
                val userDistribution = if (isGlobal) {
                    plays.groupBy { it.first }.mapValues { it.value.size }
                } else null

                val stars = when (difficulty) {
                    1 -> musicDetail?.starEasy
                    2 -> musicDetail?.starNormal
                    3 -> musicDetail?.starHard
                    4 -> musicDetail?.starOni
                    5 -> musicDetail?.starUra
                    else -> firstPlayWithName?.stars
                } ?: firstPlayWithName?.stars

                TaikoMostPlayedEntry(
                    songId = songId,
                    musicName = musicName,
                    musicNameEN = musicNameEN,
                    musicArtist = musicArtist,
                    musicArtistEN = musicArtistEN,
                    difficulty = if (difficulty > 0) difficulty else null,
                    stars = stars,
                    playCount = plays.size,
                    uniquePlayers = if (isGlobal) uniquePlayers else null,
                    userPlayCounts = userDistribution,
                    hasCurrentUserPlayed = plays.any { (user, _) -> user == myName || user == "Moi" }
                )
            }.sortedByDescending { it.playCount }.take(30)

            _mostPlayedCharts.value = mostPlayed
            _isLoadingStats.value = false
        }
    }

    private val _progressStats = MutableStateFlow<List<TaikoProgressStats>>(emptyList())
    val progressStats: StateFlow<List<TaikoProgressStats>> = _progressStats

    private val _overallProgressStats = MutableStateFlow<TaikoOverallProgressStats?>(null)
    val overallProgressStats: StateFlow<TaikoOverallProgressStats?> = _overallProgressStats

    fun fetchProgress() {
        fetchProgressForBaid(null)
    }

    fun fetchProgressForBaid(baid: Int? = null) {
        viewModelScope.launch {
            _isLoadingStats.value = true

            val historyResponse = if (baid == null) {
                _scoresData.value
            } else {
                var cached = _communityScores.value[baid]
                if (cached == null) {
                    try {
                        usecase.getPlayHistoryFlow(baid.toString()).collect { res ->
                            if (res != null) {
                                cached = res
                                val updatedMap = _communityScores.value.toMutableMap()
                                updatedMap[baid] = res
                                _communityScores.value = updatedMap
                            }
                        }
                    } catch (e: Exception) {
                        PlatformUtils.log("TaikoViewModel", "Error loading stats for baid $baid: ${e.message}")
                    }
                }
                cached
            }

            val history = historyResponse?.songHistoryData ?: emptyList()
            val musicDetails = _musicDetailsData.value ?: emptyMap()
            val totalSongsCount = musicDetails.size

            val statsList = (1..5).map { diff ->
                val scoresForDiff = history.filter { it.difficulty == diff }
                val bestScoresPerSong = scoresForDiff.groupBy { it.songId }.mapNotNull { (_, plays) ->
                    plays.maxByOrNull { it.score ?: 0 }
                }

                val playedSongs = bestScoresPerSong.size
                val clearCount = bestScoresPerSong.count { (it.crown ?: 0) >= 1 }
                val fullComboCount = bestScoresPerSong.count { (it.crown ?: 0) >= 2 }
                val donderfulCount = bestScoresPerSong.count { (it.crown ?: 0) == 3 }

                val kiwamiCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 8 }
                val miyabiGoldCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 5 }
                val miyabiPinkCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 6 }
                val miyabiPurpleCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 7 }
                val ikiWhiteCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 2 }
                val ikiBronzeCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 3 }
                val ikiBlueCount = bestScoresPerSong.count { (it.scoreRank ?: 0) == 4 }

                // Compter uniquement les chansons du catalogue qui possèdent cette difficulté
                val totalSongsForDiffInCatalog = musicDetails.values.count { detail ->
                    val stars = when (diff) {
                        1 -> detail.starEasy
                        2 -> detail.starNormal
                        3 -> detail.starHard
                        4 -> detail.starOni
                        5 -> detail.starUra
                        else -> null
                    }
                    (stars ?: 0) > 0
                }

                val finalTotalSongs = if (totalSongsForDiffInCatalog > 0) {
                    totalSongsForDiffInCatalog
                } else if (totalSongsCount > 0 && diff != 5) {
                    totalSongsCount
                } else {
                    playedSongs
                }

                TaikoProgressStats(
                    difficulty = diff,
                    totalSongs = finalTotalSongs,
                    playedSongs = playedSongs,
                    clearCount = clearCount,
                    fullComboCount = fullComboCount,
                    donderfulComboCount = donderfulCount,
                    rankKiwamiCount = kiwamiCount,
                    rankMiyabiGoldCount = miyabiGoldCount,
                    rankMiyabiPinkCount = miyabiPinkCount,
                    rankMiyabiPurpleCount = miyabiPurpleCount,
                    rankIkiWhiteCount = ikiWhiteCount,
                    rankIkiBronzeCount = ikiBronzeCount,
                    rankIkiBlueCount = ikiBlueCount
                )
            }

            // Progression globale unique (indépendante de la difficulté)
            val bestScoresPerSongOverall = history.groupBy { it.songId }.mapNotNull { (_, plays) ->
                plays.maxByOrNull { it.crown ?: 0 }
            }

            val overallPlayed = bestScoresPerSongOverall.size
            val overallClear = bestScoresPerSongOverall.count { (it.crown ?: 0) >= 1 }
            val overallFullCombo = bestScoresPerSongOverall.count { (it.crown ?: 0) >= 2 }
            val overallDonderful = bestScoresPerSongOverall.count { (it.crown ?: 0) == 3 }

            val overallStats = TaikoOverallProgressStats(
                totalSongs = if (totalSongsCount > 0) totalSongsCount else overallPlayed,
                playedSongs = overallPlayed,
                clearCount = overallClear,
                fullComboCount = overallFullCombo,
                donderfulComboCount = overallDonderful
            )

            _progressStats.value = statsList
            _overallProgressStats.value = overallStats
            _isLoadingStats.value = false
        }
    }

    suspend fun fetchPlayHistoryPlayData(userNumber: Int) {
        isLoadingScores.value = true
        try {
            usecase.getPlayHistoryFlow(userNumber.toString()).collect { response ->
                _scoresData.value = response
            }
        } catch (e: Exception) {
            isLoadingScores.value = false
        } finally {
            isLoadingScores.value = false
        }
    }

    suspend fun fetchMusicDetails() {
        isLoadingMusicDetails.value = true
        try {
            usecase.getMusicDetailsFlow().collect { response ->
                _musicDetailsData.value = response
                mergeMusicDetailsWithScores()
            }
        } catch (e: Exception) {
            isLoadingMusicDetails.value = false
        } finally {
            isLoadingMusicDetails.value = false
        }
    }

    suspend fun getUserSettings(userNumber: Int) {
        isLoadingUserSettings.value = true
        try {
            usecase.getUserSettingsFlow(userNumber.toString()).collect { response ->
                _userSettingsData.value = response
                _userDetailedSettings.value = response
            }
        } catch (e: Exception) {
            isLoadingUserSettings.value = false
        } finally {
            isLoadingUserSettings.value = false
        }
    }

    suspend fun fetchDashboard() {
        try {
            usecase.getDashboardFlow().collect { response ->
                _dashboardData.value = response
                if (response != null) {
                    checkDashboardUpdate(response)
                }
            }
        } catch (e: Exception) {
            // Silently fail or log
        }
    }

    suspend fun fetchCostumes() {
        try {
            usecase.getCostumesFlow().collect { response ->
                _costumesData.value = response
            }
        } catch (e: Exception) {}
    }

    suspend fun fetchTitles() {
        try {
            usecase.getTitlesFlow().collect { response ->
                _titlesData.value = response
            }
        } catch (e: Exception) {}
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun fetchImagesData() {
        if (_imagesData.value != null) return
        try {
            val bytes = Res.readBytes("files/taiko/images.json")
            val jsonString = bytes.decodeToString()
            val data = json.decodeFromString<TaikoImagesData>(jsonString)
            _imagesData.value = data
        } catch (e: Exception) {
            PlatformUtils.log("TaikoViewModel", "Error loading images.json: ${e.message}")
        }
    }

    private suspend fun checkDashboardUpdate(content: String) {
        val currentHash = PlatformUtils.sha256(content)
        val lastHash = userPreferencesManager.lastTaikoDashboardHash.firstOrNull()
        val showPref = userPreferencesManager.showTaikoDashboard.firstOrNull() ?: true

        if (currentHash != lastHash) {
            // New content! Force show and update hash
            userPreferencesManager.setTaikoDashboardHash(currentHash)
            userPreferencesManager.setShowTaikoDashboard(true)
            _showDashboardTrigger.value = true
        } else if (showPref) {
            // Same content, but user still wants to see it
            _showDashboardTrigger.value = true
        }
    }

    fun mergeMusicDetailsWithScores() {
        val scores = scoresData.value
        val musicDetails = musicDetailsData.value

        val mergedData = scores?.copy(
            songHistoryData = scores.songHistoryData.map { score ->
                val musicDetail = musicDetails?.get(score.songId.toString())
                if (musicDetail != null) {
                    val difficultyStars = when (score.difficulty) {
                        1 -> musicDetail.starEasy
                        2 -> musicDetail.starNormal
                        3 -> musicDetail.starHard
                        4 -> musicDetail.starOni
                        5 -> musicDetail.starUra
                        else -> 0
                    }
                    score.copy(
                        genre = musicDetail.genre ?: score.genre,
                        musicName = musicDetail.songName ?: score.musicName,
                        musicNameEN = musicDetail.songNameEN ?: score.musicNameEN,
                        musicNameCN = musicDetail.songNameCN ?: score.musicNameCN,
                        musicNameKO = musicDetail.songNameKO ?: score.musicNameKO,
                        musicArtist = musicDetail.artistName ?: score.musicArtist,
                        musicArtistEN = musicDetail.artistNameEN ?: score.musicArtistEN,
                        musicArtistCN = musicDetail.artistNameCN ?: score.musicArtistCN,
                        musicArtistKO = musicDetail.artistNameKO ?: score.musicArtistKO,
                        stars = if (difficultyStars != 0 && difficultyStars != null) difficultyStars else score.stars
                    )
                } else {
                    score
                }
            }.reversed()
        )
        _scoresData.value = mergedData
    }


    fun getCostumeImageUrl(type: String, id: Int?): String? {
        if (id == null) return null
        val paddedId = id.toString().padStart(4, '0')
        return "https://taiko.farewell.dev/images/Costumes/$type/$type-$paddedId.webp"
    }

    fun getMaskImageUrl(part: String, type: String, id: Int?): String? {
        if (id == null) return null
        val paddedId = id.toString().padStart(4, '0')
        return "https://taiko.farewell.dev/images/Costumes/masks/$part-${type}mask-$paddedId.webp"
    }

    fun getDonColor(colorIndex: Int?): Color {
        val colors = listOf(
            "#F84828", "#68C0C0", "#DC1500", "#F8F0E0", "#009687", "#00BF87",
            "#00FF9A", "#66FFC2", "#FFFFFF", "#690000", "#FF0000", "#FF6666",
            "#FFB3B3", "#00BCC2", "#00F7FF", "#66FAFF", "#B3FDFF", "#E4E4E4",
            "#993800", "#FF5E00", "#FF9E78", "#FFCFB3", "#005199", "#0088FF",
            "#66B8FF", "#B3DBFF", "#B9B9B9", "#B37700", "#FFAA00", "#FFCC66",
            "#FFE2B3", "#000C80", "#0019FF", "#6675FF", "#B3BAFF", "#858585",
            "#B39B00", "#FFDD00", "#FFFF00", "#FFFF71", "#2B0080", "#5500FF",
            "#9966FF", "#CCB3FF", "#505050", "#38A100", "#78C900", "#B3FF00",
            "#DCFF8A", "#610080", "#C400FF", "#DC66FF", "#EDB3FF", "#232323",
            "#006600", "#00B800", "#00FF00", "#8AFF9E", "#990059", "#FF0095",
            "#FF66BF", "#FFB3DF", "#000000"
        )
        val hex = if (colorIndex != null && colorIndex in colors.indices) colors[colorIndex] else "#F84828"
        return Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
    }

    fun getNameplateUrls(settings: TaikoServerUserSettingsResponse?): List<String> {
        val baseNameplatesUrl = "https://taiko.farewell.dev/images/Nameplates/"
        val layers = mutableListOf<String>()

        //if (layers.isEmpty()) {
            layers.add("${baseNameplatesUrl}nameplate.webp")
        //}

        if (settings?.isDisplayDanOnNamePlate == true) {
            layers.add("${baseNameplatesUrl}nameplate_dan.webp")
        }

        val id = settings?.titlePlateId
        if (id != null) {
            val plateSuffix = when (id) {
                0 -> "Wood"
                1 -> "Rainbow"
                2 -> "Gold"
                3 -> "Purple"
                in 4..7 -> "AI_${id - 3}"
                8 -> "Onp_1"
                9 -> "Toho_Y22_QR"
                in 10..14 -> "Toho_Y22_${id - 9}"
                in 15..20 -> "AprilFool_${id - 14}"
                else -> id.toString()
            }
            layers.add("${baseNameplatesUrl}nameplate_$plateSuffix.webp")
        }



        return layers
    }

    suspend fun login() {
        val accessCode = apiKeyManager.getTaikoAccessCode()
        val password = apiKeyManager.getTaikoPassword()
        if (accessCode != null && password != null) {
            try {
                val response = usecase.login(TaikoLoginRequest(accessCode, password))
                response.authToken?.let { token ->
                    apiKeyManager.saveTaikoAuthToken(token)
                    currentBaid = extractBaidFromToken(token)?.toIntOrNull()
                }
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Login failed: ${e.message}")
                showSnackbar("Connexion échouée : ${e.message}")
            }
        }
    }

    suspend fun updateUserSettings(settings: TaikoServerUserSettingsResponse): Boolean {
        val token = apiKeyManager.getTaikoAuthToken()
        val baid = currentBaid
        if (token != null && baid != null) {
            return try {
                usecase.updateUserSettings(baid, settings, token)
                _userSettingsData.value = settings
                _userDetailedSettings.value = settings
                true
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Update settings failed: ${e.message}")
                showSnackbar("Échec de l'enregistrement : ${e.message}")
                false
            }
        }
        return false
    }

    private val _communityScores = MutableStateFlow<Map<Int, TaikoServerPlayHistoryResponse>>(emptyMap())
    val communityScores = _communityScores

    private val _taikoUsers = MutableStateFlow<List<TaikoUser>>(emptyList())
    val taikoUsers = _taikoUsers

    fun fetchCommunityScores() {
        viewModelScope.launch {
            scorefetcherRepository.getTaikoUsers().collect { users ->
                _taikoUsers.value = users
                val scoresMap = mutableMapOf<Int, TaikoServerPlayHistoryResponse>()
                users.forEach { user ->
                    fetchUserNickname(user.baid)
                    try {
                        usecase.getPlayHistoryFlow(user.baid.toString()).collect { history ->
                            if (history != null) {
                                scoresMap[user.baid] = history
                            }
                        }
                    } catch (e: Exception) {
                        PlatformUtils.log("TaikoViewModel", "Error fetching scores for user ${user.baid}: ${e.message}")
                    }
                }
                _communityScores.value = scoresMap
            }
        }
    }

    private val fetchingNicknames = mutableSetOf<Int>()

    fun fetchUserNickname(baid: Int) {
        if (fetchingNicknames.contains(baid)) return
        val currentUser = _taikoUsers.value.find { it.baid == baid }
        if (currentUser?.nickname != null) return

        viewModelScope.launch {
            fetchingNicknames.add(baid)
            try {
                usecase.getUserSettingsFlow(baid.toString()).collect { settings ->
                    if (settings != null) {
                        val nickname = settings.myDonName
                        if (!nickname.isNullOrBlank()) {
                            _taikoUsers.value = _taikoUsers.value.map {
                                if (it.baid == baid) it.copy(nickname = nickname) else it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Error fetching nickname for $baid: ${e.message}")
            } finally {
                fetchingNicknames.remove(baid)
            }
        }
    }

    fun getScores(forceRefresh: Boolean = false) {
        if (!forceRefresh && _scoresData.value != null) {
            isLoading.value = false
            return
        }

        viewModelScope.launch {
            val accessCode = apiKeyManager.getTaikoAccessCode()
            val password = apiKeyManager.getTaikoPassword()
            
            if (accessCode != null && password != null) {
                if (forceRefresh) isRefreshing.value = true else isLoading.value = true
                
                try {
                    // 1. Login to get token and BAID
                    val authResponse = usecase.login(TaikoLoginRequest(accessCode, password))
                    val token = authResponse.authToken ?: authResponse.token
                    
                    if (token != null) {
                        apiKeyManager.saveTaikoAuthToken(token)
                        
                        // Extract BAID from token
                        currentBaid = extractBaidFromToken(token)?.toIntOrNull()
                        val baid = currentBaid ?: throw Exception("Impossible d'extraire le BAID du token")
                        
                        // 2. Fetch music details if needed
                        if (forceRefresh || _musicDetailsData.value == null) {
                            fetchMusicDetails()
                            fetchCostumes()
                            fetchTitles()
                            fetchImagesData()
                        }

                        fetchDashboard()
                        fetchPlayHistoryPlayData(baid)
                        getUserSettings(baid)
                        mergeMusicDetailsWithScores()
                    }
                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Erreur inconnue"
                    PlatformUtils.log("TaikoViewModel", "Error in getScores: $errorMsg")
                    showSnackbar("Erreur : $errorMsg")
                } finally {
                    isLoading.value = false
                    isRefreshing.value = false
                }
            } else {
                isLoading.value = false
                isRefreshing.value = false
            }
        }
    }

    private fun extractBaidFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]

            val base64 = payload
                .replace('-', '+')
                .replace('_', '/')

            val paddedBase64 = when (base64.length % 4) {
                2 -> "$base64=="
                3 -> "$base64="
                else -> base64
            }

            val decodedString = paddedBase64.decodeBase64String()
            val jsonElement = Json.parseToJsonElement(decodedString)
            val jsonObject = jsonElement.jsonObject

            val baid = jsonObject["http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"]?.jsonPrimitive?.content

            PlatformUtils.log("TaikoViewModel", "Decoded BAID: $baid")
            baid

        } catch (e: Exception) {
            PlatformUtils.log("TaikoViewModel", "Error extracting BAID from token: ${e.message}")
            null
        }
    }
}
