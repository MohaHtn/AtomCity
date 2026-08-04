package org.arcade.atomcity.di

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import org.arcade.atomcity.model.utils.JacketUrl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jacketImagesModule = module {
    single<Map<String, String>>(named("jacketImages")) { loadJacketImages(androidContext(), get()) }
}

private fun loadJacketImages(context: Context, json: Json): Map<String, String> {
    return try {
        context.assets.open("maimai/images.json").bufferedReader().use { reader ->
            val content = reader.readText()
            json.decodeFromString<List<JacketUrl>>(content).associate { it.title to it.imageUrl }
        }
    } catch (e: Exception) {
        Log.e("JacketImagesModule", "images.json for maimai song jackets not found: ${e.message}")
        emptyMap()
    }
}
