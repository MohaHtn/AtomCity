package org.arcade.atomcity.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.arcade.atomcity.data.remote.NetworkErrorHandler
import org.koin.core.qualifier.named
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase

// Helper to start Koin from iOS (Swift)
fun doInitKoin(
    scorefetcherApiKey: String,
    networkErrorHandler: NetworkErrorHandler
) {
    startKoin {
        modules(
            sharedModule,
            viewModelModule,
            iosModule,
            module {
                single(named("scorefetcher_api_key")) { scorefetcherApiKey }
                single<NetworkErrorHandler> { networkErrorHandler }
            }
        )
    }
}

object KoinProxy : KoinComponent {
    fun getMaiteaRepository(): IScorefetcherRepository = get()
    fun getApiKeyManager(): ApiKeyManager = get()
    fun getTaikoUseCase(): GetTaikoServerDataUseCase = get()
}
