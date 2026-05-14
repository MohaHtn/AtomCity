package org.arcade.atomcity.di

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.arcade.atomcity.presentation.viewmodel.JacketUrl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jacketImagesModule = module {
    single { Moshi.Builder().build() }
    single<List<JacketUrl>>(named("jacketImages")) { loadJacketImages(androidContext(), get()) }
}

private fun loadJacketImages(context: Context, moshi: Moshi): List<JacketUrl> {
    return try {
        context.assets.open("maimai/images.json").bufferedReader().use { reader ->
            val json = reader.readText()
            val type = Types.newParameterizedType(List::class.java, JacketUrl::class.java)
            val adapter = moshi.adapter<List<JacketUrl>>(type)
            adapter.fromJson(json) ?: emptyList()
        }
    } catch (e: Exception) {
        Log.e("JacketImagesModule", "images.json for maimai song jackets not found: ${e.message}")
        emptyList()
    }
}
