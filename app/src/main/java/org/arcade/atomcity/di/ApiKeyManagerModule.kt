package org.arcade.atomcity.di

import android.content.Context
import android.content.SharedPreferences
import org.arcade.atomcity.util.ApiKeyManager
import org.koin.dsl.module

val apiKeyManagerModule = module {
    single<SharedPreferences> { get<Context>().getSharedPreferences("api_prefs", Context.MODE_PRIVATE) }
    single { ApiKeyManager(get()) }
}