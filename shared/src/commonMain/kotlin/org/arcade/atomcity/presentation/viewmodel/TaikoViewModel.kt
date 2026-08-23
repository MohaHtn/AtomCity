package org.arcade.atomcity.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.UserPreferencesManager
import org.arcade.atomcity.utils.PlatformUtils
import kotlinx.coroutines.flow.firstOrNull

class TaikoViewModel(
    private val usecase: GetTaikoServerDataUseCase,
    private val apiKeyManager: ApiKeyManager,
    private val userPreferencesManager: UserPreferencesManager
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    val filteredScores: StateFlow<List<org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry>> = combine(scoresData, _searchQuery) { scores, query ->
        if (query.isBlank()) {
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



    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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
        // Basic guess for Taiko colors - needs actual mapping
        return when (colorIndex) {
            0 -> Color(0xFFF84828) // Red
            1 -> Color(0xFFF8B800) // Yellow
            2 -> Color(0xFF60B048) // Green
            3 -> Color(0xFF0098D8) // Blue
            4 -> Color(0xFFE85888) // Pink
            5 -> Color(0xFF8840A8) // Purple
            6 -> Color(0xFFFFFFFF) // White
            7 -> Color(0xFF000000) // Black
            else -> Color.Gray
        }
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

    fun getScores(forceRefresh: Boolean = false) {
        if (!forceRefresh && _scoresData.value != null) return

        viewModelScope.launch {
            val userIdString = apiKeyManager.getApiKey("taiko")
            val userId = userIdString?.toIntOrNull()

            if (userId != null) {
                if (forceRefresh) isRefreshing.value = true else isLoading.value = true
                
                // Only fetch music details if not already present or if forced
                if (forceRefresh || _musicDetailsData.value == null) {
                    fetchMusicDetails()
                    fetchCostumes()
                    fetchTitles()
                }
                
                fetchDashboard()
                fetchPlayHistoryPlayData(userId)
                getUserSettings(userId)
                mergeMusicDetailsWithScores()
                
                isLoading.value = false
                isRefreshing.value = false
            }
        }
    }
}
