package org.arcade.atomcity.di

import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { ScorefetcherViewModel(get(), get(), get(), get(), get()) }
    factory { TaikoViewModel(get(), get()) }
}
