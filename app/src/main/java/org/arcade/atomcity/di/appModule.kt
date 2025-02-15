// appModule.kt
package org.arcade.atomcity.di

import android.content.Context
import org.koin.dsl.module

val appModule = module {
    single { provideContext(get()) }
}

fun provideContext(context: Context): Context = context