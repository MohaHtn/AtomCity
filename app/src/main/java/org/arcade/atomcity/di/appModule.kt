package org.arcade.atomcity.di

import android.content.Context
import androidx.work.WorkManager
import org.koin.dsl.module

val appModule = module {
    single<WorkManager> { WorkManager.getInstance(get<Context>()) }
}
