package org.arcade.atomcity.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "api_keys")

val apiKeyManagerModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
}
