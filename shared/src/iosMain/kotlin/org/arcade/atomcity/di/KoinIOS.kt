package org.arcade.atomcity.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.arcade.atomcity.network.NetworkErrorHandler
import org.koin.core.qualifier.named

// Helper to start Koin from iOS (Swift)
fun initKoin(
    scorefetcherApiKey: String,
    networkErrorHandler: NetworkErrorHandler
) {
    startKoin {
        modules(
            sharedModule,
            module {
                single(named("scorefetcher_api_key")) { scorefetcherApiKey }
                single<NetworkErrorHandler> { networkErrorHandler }
            }
        )
    }
}
