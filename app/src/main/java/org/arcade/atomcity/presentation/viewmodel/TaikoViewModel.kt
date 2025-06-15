package org.arcade.atomcity.presentation.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.net.URL
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaikoViewModel(private val usecase: GetTaikoServerDataUseCase) : ViewModel() {

    // StateFlow to hold the music details data
    private val _scoresData = MutableStateFlow<TaikoServerPlayHistoryResponse?>(null)
    val scoresData = _scoresData

    // StateFlow to hold the music details data
    private val _musicDetailsData = MutableStateFlow<TaikoServerMusicDetailsResponse?>(null)
    val musicDetailsData = _musicDetailsData

    // StateFlow to hold the user settings data
    private val _userSettingsData = MutableStateFlow<TaikoServerUserSettingsResponse?>(null)
    val userSettingsData = _userSettingsData

    val isLoading = MutableStateFlow(false)
    val isLoadingMusicDetails = MutableStateFlow(false)
    val isLoadingUserSettings = MutableStateFlow(false)
    val isLoadingScores = MutableStateFlow(false)

    internal val _currentPage = MutableStateFlow(1)

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    suspend fun fetchPlayHistoryPlayData(page: Int) {
        isLoadingScores.value = true
        try {
            usecase.getPlayHistoryFlow(page.toString()).collect { response ->
                _scoresData.value = response
            }
        } catch (e: Exception) {
            Log.e("TaikoScoresViewModel", "Error: ${e.message}")
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
            Log.e("TaikoScoresViewModel", "Error: ${e.message}")
            isLoadingMusicDetails.value = false
        } finally {
            isLoadingMusicDetails.value = false
        }
    }

    suspend fun getUserSettings() {
        isLoadingUserSettings.value = true
        try {
            usecase.getUserSettingsFlow().collect { response ->
                _userSettingsData.value = response
            }
        } catch (e: Exception) {
            Log.e("TaikoViewModel", "Error fetching user settings: ${e.message}")
            isLoadingUserSettings.value = false
        } finally {
            isLoadingUserSettings.value = false
        }
    }

    fun mergeMusicDetailsWithScores() {
        val scores = scoresData.value
        val musicDetails = musicDetailsData.value

        val mergedData = scores!!.copy(
            taikoServerSongHistoryData = scores.taikoServerSongHistoryData.map { score ->
                val musicDetail = musicDetails?.entries?.find { it.key == score.songId.toString() }?.value
                if (musicDetail != null) {
                    score.copy(
                        musicName = musicDetail.songName,
                        musicArtist = musicDetail.artistName
                    )
                } else {
                    score
                }
            }.reversed()
        )
        _scoresData.value = mergedData
    }


    fun getScores() {
        viewModelScope.launch {
            fetchMusicDetails()
            fetchPlayHistoryPlayData(1)
            getUserSettings()
            mergeMusicDetailsWithScores()
            getAvatar()
        }
    }

    @SuppressLint("DefaultLocale")
    fun getAvatar() {
        viewModelScope.launch {
            val kigurumi = userSettingsData.value?.kigurumi?.let { String.format("%04d", it) } ?: "0000"
            val head = userSettingsData.value?.head?.let { String.format("%04d", it) } ?: "0000"
            val body = userSettingsData.value?.body?.let { String.format("%04d", it) } ?: "0000"
            val face = userSettingsData.value?.face?.let { String.format("%04d", it) } ?: "0000"
            val puchi = userSettingsData.value?.puchi?.let { String.format("%04d", it) } ?: "0000"

            val bodyUrl = "https://taiko.farewell.dev/images/Costumes/body/body-$body.webp"
            val headUrl = "https://taiko.farewell.dev/images/Costumes/head/head-$head.webp"
            val faceUrl = "https://taiko.farewell.dev/images/Costumes/face/face-$face.webp"
            val puchiUrl = "https://taiko.farewell.dev/images/Costumes/puchi/puchi-$puchi.webp"

            val avatarUrls = listOf(bodyUrl, headUrl, faceUrl, puchiUrl)
            loadMergedAvatar(avatarUrls)
        }
    }

    val mergedAvatar = MutableStateFlow<Bitmap?>(null)

    fun loadMergedAvatar(urls: List<String>) {
        viewModelScope.launch {
            val bitmap = mergeImages(urls)
            mergedAvatar.value = bitmap
        }
    }

    suspend fun mergeImages(urls: List<String>): Bitmap = withContext(Dispatchers.IO) {
        val bitmaps: List<Bitmap> = urls.map { url ->
            val input = URL(url).openStream()
            BitmapFactory.decodeStream(input)
        }
        if (bitmaps.isEmpty()) {
            throw IllegalArgumentException("No bitmaps provided to merge.")
        }
        val result: Bitmap = createBitmap(
            bitmaps[0].width,
            bitmaps[0].height,
            bitmaps[0].config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        bitmaps.forEach { bitmap -> canvas.drawBitmap(bitmap, 0f, 0f, null) }
        result
    }
}
