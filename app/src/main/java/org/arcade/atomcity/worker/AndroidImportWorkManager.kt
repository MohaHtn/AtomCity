package org.arcade.atomcity.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class AndroidImportWorkManager(private val context: Context) : ImportWorkManager {
    private val workManager = WorkManager.getInstance(context)
    private val IMPORT_WORK_NAME = "maimai_import_work"

    override fun startImport(apiKey: String) {
        val workRequest = OneTimeWorkRequestBuilder<MaimaiImportWorker>()
            .setInputData(MaimaiImportWorker.createInputData(apiKey))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10L,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueueUniqueWork(
            IMPORT_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun isImportActive(): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(IMPORT_WORK_NAME).map { workInfos ->
            val latest = workInfos.lastOrNull()
            latest?.state == WorkInfo.State.ENQUEUED || 
            latest?.state == WorkInfo.State.RUNNING || 
            latest?.state == WorkInfo.State.BLOCKED
        }
    }

    override fun observeProgress(): Flow<ImportProgress?> {
        return workManager.getWorkInfosForUniqueWorkFlow(IMPORT_WORK_NAME).map { workInfos ->
            workInfos.lastOrNull()?.let { workInfo ->
                ImportProgress(
                    state = workInfo.state.name.lowercase(),
                    progress = workInfo.progress.getInt("progress", 0),
                    message = workInfo.progress.getString("message")
                )
            }
        }
    }
}
