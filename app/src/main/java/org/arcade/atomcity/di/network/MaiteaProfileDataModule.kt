package org.arcade.atomcity.di.network

import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.arcade.atomcity.network.MaiteaProfileService
import org.arcade.atomcity.network.MaiteaService
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val maiteaProfileDataModule =  module {
    single<Retrofit>(named("maitea_profile")) {
        val context = androidContext()
        val apiKeyManager = ApiKeyManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain: Interceptor.Chain ->
                val apiKey = runBlocking { apiKeyManager.getApiKey("maimai") }
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(300, TimeUnit.SECONDS) // Délai d'attente de connexion
            .readTimeout(300, TimeUnit.SECONDS)    // Délai d'attente de lecture
            .writeTimeout(300, TimeUnit.SECONDS)   // Délai d'attente d'écriture
            .build()

        Retrofit.Builder()
            .baseUrl("https://maitea.app/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<MaiteaProfileService> {
        get<Retrofit>(named("maitea_profile")).create(MaiteaProfileService::class.java)
    }
}