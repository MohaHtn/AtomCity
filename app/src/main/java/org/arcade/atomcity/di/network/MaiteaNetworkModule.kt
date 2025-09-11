package org.arcade.atomcity.di.network

import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import org.arcade.atomcity.network.MaiteaService
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import java.util.concurrent.TimeUnit

val maiteaNetworkModule = module {
    single<Retrofit>(named("maitea_scores")) {
        val context = androidContext()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
                .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-KEY", "ut52IrahAF7mTF9eIgSchTvjkWziKB1J1MWFewaFfcZrQmvUxZEi3InGhDSwbV5zklkBqXRUjuXksFMJ1gfaQlxCeBsghbB1rc68kH46IW9q93HvU2oWU1WIPDCOviAh")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://scorefetcher.mohahtn.xyz")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<MaiteaService> {
        get<Retrofit>(named("maitea_scores")).create(MaiteaService::class.java)
    }
}