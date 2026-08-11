package org.arcade.atomcity.di

import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { MaiteaViewModel(get()) }
    factory { TaikoViewModel(get(), get()) }
}
