package org.arcade.atomcity.di

import org.arcade.atomcity.BuildConfig
import org.arcade.atomcity.data.remote.AndroidNetworkErrorHandler
import org.arcade.atomcity.data.remote.NetworkErrorHandler
import org.arcade.atomcity.ui.core.GlobalUIState
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<NetworkErrorHandler> { AndroidNetworkErrorHandler() }
    single { GlobalUIState }
    single(named("scorefetcher_api_key")) { BuildConfig.SCOREFETCHER_API_KEY }
}
