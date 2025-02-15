package org.arcade.atomcity.di

import android.content.Context
import okhttp3.Dns
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import org.arcade.atomcity.network.MaiteaApiService
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import org.arcade.atomcity.util.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetAddress
import java.util.concurrent.TimeUnit

val maiteaNetworkModule = module {
    single<Retrofit> {
        val context = androidContext()
        val apiKey = ApiKeyManager.getApiKey(context) ?: throw IllegalStateException("API key not found")

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val moshi = com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()


        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer 377|9TdBVuvl96tWpBFezkbCUwZ57aM6gDGAeAjEpMaz")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS) // Délai d'attente de connexion
            .readTimeout(30, TimeUnit.SECONDS)    // Délai d'attente de lecture
            .writeTimeout(30, TimeUnit.SECONDS)   // Délai d'attente d'écriture
            .build()

        Retrofit.Builder()
            .baseUrl("https://maitea.app/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<MaiteaApiService> {
        get<Retrofit>().create(MaiteaApiService::class.java)
    }
}