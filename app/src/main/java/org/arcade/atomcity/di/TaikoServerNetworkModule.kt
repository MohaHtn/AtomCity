package org.arcade.atomcity.di

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.arcade.atomcity.network.MaiteaApiService
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val maiteaNetworkModule = module {
    single<Retrofit> {
        val context = androidContext()
        val apiKeyManager = ApiKeyManager(context)
        val apiKey = runBlocking { apiKeyManager.getApiKey("taiko") }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val moshi = com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(300, TimeUnit.SECONDS) // Délai d'attente de connexion
            .readTimeout(300, TimeUnit.SECONDS)    // Délai d'attente de lecture
            .writeTimeout(300, TimeUnit.SECONDS)   // Délai d'attente d'écriture
            .build()

        Retrofit.Builder()
            .baseUrl("https://taiko.farewell.dev/api")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<MaiteaApiService> {
        get<Retrofit>().create(MaiteaApiService::class.java)
    }
}