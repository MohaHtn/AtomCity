package org.arcade.atomcity.di

import org.arcade.atomcity.worker.AndroidImportWorkManager
import org.arcade.atomcity.worker.ImportWorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val workerModule = module {
    single<ImportWorkManager> { AndroidImportWorkManager(androidContext()) }
}
