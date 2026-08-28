package org.arcade.atomcity.di

import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { MaimaiViewModel(get(), get(), get(), get(), get()) }
    factory { TaikoViewModel(get(), get(), get(), get()) }
}
