package org.arcade.atomcity.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.arcade.atomcity.BuildConfig
import org.arcade.atomcity.utils.PlatformUtils
import java.util.concurrent.TimeUnit

class MaimaiImportWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "maimai_import_channel"
        const val NOTIFICATION_ID = 1001
        const val KEY_API_KEY = "api_key"

        fun createInputData(apiKey: String) = workDataOf(
            KEY_API_KEY to apiKey
        )
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return createForegroundInfo(0, "Initialisation de l'import ...")
    }

    private suspend fun updateProgress(progress: Int, message: String) {
        setProgress(workDataOf(
            "progress" to progress,
            "message" to message
        ))
        setForeground(createForegroundInfo(progress, message))
    }

    override suspend fun doWork(): Result {
        val apiKey = inputData.getString(KEY_API_KEY)?.trim() ?: return Result.failure()
        val keyHash = PlatformUtils.sha256(apiKey)

        createNotificationChannel()
        updateProgress(0, "Lancement de l'import ...")

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("https://scorefetcher.mohahtn.xyz/imports/$keyHash/events")
            .addHeader("X-API-KEY", BuildConfig.SCOREFETCHER_API_KEY)
            .addHeader("Accept", "text/event-stream")
            .build()

        val json = Json { ignoreUnknownKeys = true }

        val maxReconnectAttempts = 5
        repeat(maxReconnectAttempts) { attempt ->
            val shouldRetry = try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        false
                    } else {
                        val body = response.body ?: return@use false
                        val source = body.source()
                        var completed = false
                        var sawPageEvent = false
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (line.startsWith("data:")) {
                                val jsonData = line.removePrefix("data:").trim()
                                val event = try {
                                    json.decodeFromString<MaimaiImportEvent>(jsonData)
                                } catch (_: Exception) {
                                    null
                                }

                                if (event != null) {
                                    if (event.type == "page") {
                                        sawPageEvent = true
                                        val progress = if ((event.totalPages ?: 0) > 0) {
                                            (((event.page ?: 0).toFloat() / (event.totalPages ?: 1)) * 100)
                                                .toInt()
                                                .coerceIn(0, 100)
                                        } else {
                                            0
                                        }
                                        updateProgress(
                                            progress,
                                            "Importation en cours : ${progress}% (Page ${event.page} / ${event.totalPages})"
                                        )

                                        if ((event.page ?: 0) >= (event.totalPages ?: 0)) {
                                            completed = true
                                            return@use true
                                        }
                                    } else if (event.type == "completed") {
                                        return@use true
                                    } else if (event.type == "failed") {
                                        return@use false
                                    }
                                }
                            }
                        }

                        if (sawPageEvent && !completed) {
                            completed = true
                        }

                        completed
                    }
                }
            } catch (e: Exception) {
                updateProgress(
                    0,
                    "Connexion perdue, reconnexion ${attempt + 1}/$maxReconnectAttempts..."
                )
                false
            }

            if (shouldRetry) {
                showFinalNotification("L'importation s'est bien déroulé.")
                return Result.success()
            }

            if (attempt < maxReconnectAttempts - 1) {
                delay(5_000L)
            }
        }

        showFinalNotification("Import failed: impossible de maintenir la connexion à l'endpoint d'import.")
        return Result.retry()
    }

    private fun showFinalNotification(message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Importation terminé !")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createForegroundInfo(progress: Int, message: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Préparation • Importation de tous les scores maimai FiNALE ...")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Import de tous les scores maimai FiNALE",
                NotificationManager.IMPORTANCE_DEFAULT 
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
