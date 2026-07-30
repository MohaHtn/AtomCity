package org.arcade.atomcity.di

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.buffer
import okio.source
import org.arcade.atomcity.presentation.viewmodel.JacketUrl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jacketImagesModule = module {
    single { Moshi.Builder().build() }
    single<Map<String, String>>(named("jacketImages")) { loadJacketImages(androidContext(), get()) }
}

private fun loadJacketImages(context: Context, moshi: Moshi): Map<String, String> {
    return try {
        context.assets.open("maimai/images.json").source().buffer().use { source ->
            val type = Types.newParameterizedType(List::class.java, JacketUrl::class.java)
            val adapter = moshi.adapter<List<JacketUrl>>(type)
            adapter.fromJson(source)?.associate { it.title to it.imageUrl } ?: emptyMap()
        }
    } catch (e: Exception) {
        Log.e("JacketImagesModule", "images.json for maimai song jackets not found: ${e.message}")
        emptyMap()
    }
}
