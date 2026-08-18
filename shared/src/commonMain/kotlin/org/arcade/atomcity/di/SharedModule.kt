package org.arcade.atomcity.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.arcade.atomcity.network.ScorefetcherClient
import org.arcade.atomcity.network.TaikoServerClient
import org.arcade.atomcity.network.ScorefetcherProfileClient
import org.arcade.atomcity.network.ImportService
import org.arcade.atomcity.network.NetworkErrorHandler
import org.arcade.atomcity.network.installErrorValidator
import org.arcade.atomcity.db.AppDatabase
import org.arcade.atomcity.db.getAppDatabase
import org.arcade.atomcity.db.getDatabaseBuilder
import org.koin.dsl.module
import org.arcade.atomcity.data.ScorefetcherRepository
import org.arcade.atomcity.data.TaikoServerRepository
import org.arcade.atomcity.data.DifficultyRepository
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.ThemeSettingsManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.worker.ImportWorkManager
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.domain.usecase.*
import org.koin.core.qualifier.named
import org.arcade.atomcity.model.utils.JacketUrl
import org.arcade.atomcity.model.utils.MAIMAI_IMAGES_JSON

val sharedModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        val errorHandler: NetworkErrorHandler = get()
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        PlatformUtils.log("HTTP Client", message)
                    }
                }
                level = LogLevel.ALL
            }
            installErrorValidator(errorHandler)
        }
    }

    single { ScorefetcherClient(get(), get(named("scorefetcher_api_key")), "https://scorefetcher.mohahtn.xyz/") }
    single { TaikoServerClient(get(), "https://taiko.farewell.dev/api/") }
    single { ScorefetcherProfileClient(get(), "https://maitea.app/api/v1/") }
    single { ImportService(get(), get(named("scorefetcher_api_key"))) }

    single<Map<String, String>>(named("jacketImages")) {
        try {
            get<Json>().decodeFromString<List<JacketUrl>>(MAIMAI_IMAGES_JSON)
                .associate { it.title to it.imageUrl }
        } catch (e: Exception) {
            PlatformUtils.log("SharedModule", "Error parsing jacket images: ${e.message}", true)
            emptyMap()
        }
    }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder()) }
    single<com.atomcity.maimai.db.AppDatabase> { com.atomcity.maimai.db.getAppDatabase(com.atomcity.maimai.db.getDatabaseBuilder()) }

    single { GetTaikoServerDataUseCase(get()) }
    single { GetScorefetcherScoresUseCase(get()) }
    single { GetScorefetcherProfileUseCase(get()) }
    single { ScorefetcherImportUseCase(get()) }
    single { GetScorefetcherAnalyticsUseCase(get()) }
    single { GetScorefetcherJacketUseCase(get()) }
    single { TaikoServerRepository(get()) }
    single { DifficultyRepository(get()) }
    single { ApiKeyManager(get()) }
    single { ThemeSettingsManager(get()) }
    single { ScorefetcherRepository(get(), get(), get(), get(), get(), get(), get(), get(named("jacketImages")), get(named("scorefetcher_api_key"))) }
}
