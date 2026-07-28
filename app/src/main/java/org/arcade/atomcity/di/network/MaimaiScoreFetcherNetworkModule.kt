package org.arcade.atomcity.di.network

import com.squareup.moshi.Moshi
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import org.arcade.atomcity.network.ScorefetcherService
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.arcade.atomcity.BuildConfig

val maiteaNetworkModule = module {
    single<Retrofit>(named("maitea_scores")) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }



        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-KEY", BuildConfig.SCOREFETCHER_API_KEY)
                    .build()
                chain.proceed(request)
            }
            //.addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
             .baseUrl("https://scorefetcher.mohahtn.xyz")
            //.baseUrl("http://192.168.1.132:8080")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single<ScorefetcherService> {
        get<Retrofit>(named("maitea_scores")).create(ScorefetcherService::class.java)
    }
}