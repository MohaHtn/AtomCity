package org.arcade.atomcity.ui.game.common

import android.content.Context
import android.util.Log
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStreamReader

fun selectRatingBackgroundColor(rating: Int?): Color {
    return when (rating) {
        null -> Color.Transparent
        in 0..200 -> Color.White
        in 200..399 -> Color.Blue
        in 400..699 -> Color.Green
        in 700..999 -> Color.Cyan
        in 1000..1199 -> Color.Red
        in 1200..1299 -> Color.Magenta
        in 1300..1399 -> Color(0xFFA52A2A) // Brown
        in 1400..1449 -> Color.Gray
        in 1450..1499 -> Color.Yellow
        else -> Color.Black
    }
}

@Composable
fun getDifficultyColorBackground(difficulty: String?): CardColors {
    return when (difficulty) {
        "easy" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFE1F5FE))
        "basic" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFF1F8E9))
        "advanced" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF8E1))
        "expert" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFEBEE))
        "master" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3E5F5))
        "remaster" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFCE4EC))
        else -> CardDefaults.elevatedCardColors()
    }
}

fun getJacketBorderColor(difficulty: String?): Color {
    return when (difficulty) {
        "easy" -> Color(0xFF03A9F4) // Deep Light Blue
        "basic" -> Color(0xFF4CAF50) // Strong Green
        "advanced" -> Color(0xFFFBC02D) // Strong Yellow
        "expert" -> Color(0xFFF44336) // Strong Red
        "master" -> Color(0xFF9C27B0) // Deep Purple
        "remaster" -> Color(0xFFFF4081) // Vibrant Pink (Re:Master)
        else -> Color.Transparent
    }
}

@Composable
fun getDifficultyLevelFromCSV(context: Context, songName: String, difficulty: String?): String {
    // Checking the cache first
    val cacheKey = Pair(songName, difficulty)
    difficultyCache[cacheKey]?.let { return it }

    // Load CSV data if not already loaded
    if (!csvDataLoaded) {
        try {
            loadCsvData(context)
        } catch (e: Exception) {
            Log.e("MaimaiScoreDetails", "Erreur lors du chargement du fichier CSV", e)
        }
    }

    // Else, retrieve the difficulty level from the CSV data
    val songData = csvData[songName.lowercase()]
    Log.d("MaimaiScoreDetails", "Searching for song: $songName with difficulty: $difficulty")
    Log.d("MaimaiScoreDetails", songData.toString())
    val difficultyValue = when (difficulty?.lowercase()) {
        "easy" -> songData?.get("easy")
        "basic" -> songData?.get("basic")
        "advanced" -> songData?.get("advanced")
        "expert" -> songData?.get("expert")
        "master" -> songData?.get("master")
        "remaster" -> songData?.get("remaster")
        else -> null
    }

    val result = if (difficultyValue != null && difficultyValue != "-") difficultyValue else "N/A"

    // Store the result in the cache
    difficultyCache[cacheKey] = result

    return result
}

// Good'ol cache to avoid reloading the CSV file multiple times
private val difficultyCache = mutableMapOf<Pair<String, String?>, String>()
private var csvDataLoaded = false
private val csvData = mutableMapOf<String, Map<String, String>>()


private fun loadCsvData(context: Context) {
    context.resources.assets.open("maimai/songs.csv").use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->

            // Skip header
            reader.readLine()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val values = line?.split(",")

                if (values != null && values.size >= 8) {
                    val songName = values[0].trim().lowercase()
                    val difficulties = mapOf(
                        "easy" to values[2].trim(),
                        "basic" to values[3].trim(),
                        "advanced" to values[4].trim(),
                        "expert" to values[5].trim(),
                        "master" to values[6].trim(),
                        "remaster" to values[7].trim()
                    )
                    csvData[songName] = difficulties
                }
            }
        }
    }
    csvDataLoaded = true
}