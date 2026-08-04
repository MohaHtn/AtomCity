package org.arcade.atomcity.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.arcade.atomcity.network.NetworkErrorHandler
import org.koin.core.qualifier.named
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.arcade.atomcity.data.MaiteaRepository

// Helper to start Koin from iOS (Swift)
fun initKoin(
    scorefetcherApiKey: String,
    networkErrorHandler: NetworkErrorHandler
) {
    startKoin {
        modules(
            sharedModule,
            iosModule,
            module {
                single(named("scorefetcher_api_key")) { scorefetcherApiKey }
                single<NetworkErrorHandler> { networkErrorHandler }
            }
        )
    }
}

object KoinProxy : KoinComponent {
    fun getMaiteaRepository(): MaiteaRepository = get()
}
