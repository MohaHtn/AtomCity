package org.arcade.atomcity.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.arcade.atomcity.BuildConfig
import org.arcade.atomcity.data.remote.NetworkErrorHandler
import org.arcade.atomcity.network.android.AndroidNetworkErrorHandler
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.worker.AndroidImportWorkManager
import org.arcade.atomcity.worker.ImportWorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "api_keys")

val appModule = module {
    single<NetworkErrorHandler> { AndroidNetworkErrorHandler() }
    single { GlobalUIState }
    single(named("scorefetcher_api_key")) { BuildConfig.SCOREFETCHER_API_KEY }

    // From ApiKeyManagerModule
    single<DataStore<Preferences>> { androidContext().dataStore }

    // From WorkerModule
    single<ImportWorkManager> { AndroidImportWorkManager(androidContext()) }

    // From TaikoServerViewModelModule
    viewModelOf(::TaikoViewModel)

    // From ScorefetcherViewModelModule
    viewModel {
        MaimaiViewModel(
            scoresUseCase = get(),
            profileUseCase = get(),
            importUseCase = get(),
            analyticsUseCase = get(),
            jacketUseCase = get()
        )
    }
}
