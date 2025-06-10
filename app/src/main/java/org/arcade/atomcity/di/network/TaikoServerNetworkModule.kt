package org.arcade.atomcity.di.network

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.arcade.atomcity.network.TaikoServerService
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val taikoNetworkModule = module {
    single<Retrofit>(named("taiko")) {
        val context = androidContext()
//        val apiKeyManager = ApiKeyManager(context)
//        val apiKey = runBlocking { apiKeyManager.getApiKey("taiko") }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder()
/*
            .addInterceptor(loggingInterceptor)
*/
            .connectTimeout(300, TimeUnit.SECONDS) // Délai d'attente de connexion
            .readTimeout(300, TimeUnit.SECONDS)    // Délai d'attente de lecture
            .writeTimeout(300, TimeUnit.SECONDS)   // Délai d'attente d'écriture
            .build()

        Retrofit.Builder()
            .baseUrl("https://taiko.farewell.dev/api/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<TaikoServerService> {
        get<Retrofit>(named("taiko")).create(TaikoServerService::class.java)
    }

}