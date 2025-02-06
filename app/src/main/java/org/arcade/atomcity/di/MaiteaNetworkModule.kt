package org.arcade.atomcity.di

import android.content.Context
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import org.arcade.atomcity.network.MaiteaApiService
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import org.arcade.atomcity.util.ApiKeyManager

val maiteaNetworkModule = module {
    single<Retrofit> { (context: Context) ->
        val apiKey = ApiKeyManager.getApiKey(context) ?: throw IllegalStateException("API key not found")

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://maitea.app/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    single<MaiteaApiService> {
        get<Retrofit>().create(MaiteaApiService::class.java)
    }
}