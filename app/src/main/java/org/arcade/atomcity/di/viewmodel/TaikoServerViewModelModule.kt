package org.arcade.atomcity.di.viewmodel

import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val taikoServerViewModelModule = module {
    viewModelOf(::TaikoViewModel)
}