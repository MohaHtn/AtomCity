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
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.arcade.atomcity.BuildConfig
import java.security.MessageDigest

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

    override suspend fun doWork(): Result {
        val apiKey = inputData.getString(KEY_API_KEY) ?: return Result.failure()
        val keyHash = sha256(apiKey)

        createNotificationChannel()
        setForeground(createForegroundInfo(0, "Lancement de l'import ..."))

        val client = OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("https://scorefetcher.mohahtn.xyz/apikeys/imports/$keyHash/events")
            .addHeader("X-API-KEY", BuildConfig.SCOREFETCHER_API_KEY)
            .addHeader("Accept", "text/event-stream")
            .build()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(MaimaiImportEvent::class.java)

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure()

                val source = response.body.source()
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data:")) {
                        val jsonData = line.removePrefix("data:").trim()
                        val event = try {
                            adapter.fromJson(jsonData)
                        } catch (_: Exception) {
                            null
                        }

                        if (event != null && event.type == "page") {
                            val progress = if (event.totalPages > 0) {
                                ((event.page.toFloat() / event.totalPages) * 100)
                                    .toInt()
                                    .coerceIn(0, 100)
                            } else {
                                0
                            }
                            setForeground(
                                createForegroundInfo(
                                    progress,
                                    "Importation en cours : ${progress}% (Page ${event.page} / ${event.totalPages})"
                                )
                            )

                            if (event.page >= event.totalPages) {
                                break
                            }
                        }
                    }
                }
                showFinalNotification("L'importation s'est bien déroulé.")
                Result.success()
            }
        } catch (e: Exception) {
            showFinalNotification("Import failed: ${e.message}")
            Result.retry()
        }
    }

    private fun showFinalNotification(message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            //TODO: i18n
            .setContentTitle("Importation terminé !")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun createForegroundInfo(progress: Int, message: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            //TODO: i18n
            .setContentTitle("Préparation • Importation de tous les scores maimai FiNALE ...")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setSilent(true) // Don't make sound for every progress update
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
                //TODO:i18n
                "Import de tous les scores maimai FiNALE",
                NotificationManager.IMPORTANCE_DEFAULT // Default importance to show in status bar
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@JsonClass(generateAdapter = true)
data class MaimaiImportEvent(
    val type: String,
    val keyHash: String,
    val page: Int,
    val totalPages: Int,
    val message: String
)
