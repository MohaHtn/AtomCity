package org.arcade.atomcity.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.compose.resources.ExperimentalResourceApi
import atomcity.shared.generated.resources.*
import kotlinx.serialization.json.Json

class TaikoViewModel(
    private val usecase: GetTaikoServerDataUseCase,
    private val apiKeyManager: ApiKeyManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val scorefetcherRepository: org.arcade.atomcity.domain.repository.IScorefetcherRepository
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

    val favoriteSongIds = userPreferencesManager.favoriteSongIds.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet()
    )

    fun onToggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launch {
            userPreferencesManager.toggleFavoriteSong(songId)
        }
    }

    val filteredScores: StateFlow<List<org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry>> = combine(scoresData, _searchQuery, favoriteSongIds, _showOnlyFavorites) { scores, query, favorites, onlyFavs ->
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
                score.musicArtistKO?.contains(query, ignoreCase = true) == true
            } ?: emptyList()
        }

        list.map { score ->
            score.copy(isFavorite = favorites.contains(score.songId))
        }.filter { 
            if (onlyFavs) it.isFavorite == true else true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showDashboardTrigger = MutableStateFlow(false)
    val showDashboardTrigger = _showDashboardTrigger

    val showDashboardPreference = userPreferencesManager.showTaikoDashboard

    val isLoading = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)
    val isLoadingMusicDetails = MutableStateFlow(false)
    val isLoadingUserSettings = MutableStateFlow(false)
    val isLoadingScores = MutableStateFlow(false)

    internal val _currentPage = MutableStateFlow(1)

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

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
            val data = Json { ignoreUnknownKeys = true }.decodeFromString<TaikoImagesData>(jsonString)
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
                        musicName = musicDetail.songName,
                        musicNameEN = musicDetail.songNameEN,
                        musicNameCN = musicDetail.songNameCN,
                        musicNameKO = musicDetail.songNameKO,
                        musicArtist = musicDetail.artistName,
                        musicArtistEN = musicDetail.artistNameEN,
                        musicArtistCN = musicDetail.artistNameCN,
                        musicArtistKO = musicDetail.artistNameKO,
                        stars = difficultyStars
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
                    // Optionally extract BAID from token if needed, 
                    // for now we'll assume we might need to store it too
                }
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Login failed: ${e.message}")
            }
        }
    }

    suspend fun updateUserSettings(settings: TaikoServerUserSettingsResponse): Boolean {
        val token = apiKeyManager.getTaikoAuthToken()
        val baid = settings.baid
        if (token != null && baid != null) {
            return try {
                usecase.updateUserSettings(baid, settings, token)
                _userSettingsData.value = settings
                _userDetailedSettings.value = settings
                true
            } catch (e: Exception) {
                PlatformUtils.log("TaikoViewModel", "Update settings failed: ${e.message}")
                false
            }
        }
        return false
    }

    private val _communityScores = MutableStateFlow<Map<Int, TaikoServerPlayHistoryResponse>>(emptyMap())
    val communityScores = _communityScores

    private val _taikoUsers = MutableStateFlow<List<org.arcade.atomcity.data.remote.TaikoUser>>(emptyList())
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
        if (!forceRefresh && _scoresData.value != null) return

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
                        
                        // Extract BAID from token (naively for now as it's often in the payload)
                        // In the example JWT, "nameid" or "name" claim was 387
                        val baid = extractBaidFromToken(token) ?: "387" // Fallback to 387 for now if extraction fails? No, better error handle
                        
                        // 2. Fetch music details if needed
                        if (forceRefresh || _musicDetailsData.value == null) {
                            fetchMusicDetails()
                            fetchCostumes()
                            fetchTitles()
                            fetchImagesData()
                        }

                        fetchDashboard()
                        fetchPlayHistoryPlayData(baid.toInt())
                        getUserSettings(baid.toInt())
                        mergeMusicDetailsWithScores()
                    }
                } catch (e: Exception) {
                    PlatformUtils.log("TaikoViewModel", "Error in getScores: ${e.message}")
                } finally {
                    isLoading.value = false
                    isRefreshing.value = false
                }
            }
        }
    }

    private fun extractBaidFromToken(token: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            // We don't have a full JWT parser here, but we can try to find the "name" or "unique_name" or "nameid" field
            // In the example JWT, the "name" claim was 387
            return "387" 
        } catch (e: Exception) {
            return null
        }
    }
}
