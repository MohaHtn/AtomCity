package org.arcade.atomcity.di

import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.domain.usecase.GetMaiteatDataUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    single { GetMaiteatDataUseCase(get()) }
    viewModelOf(::MainActivityViewModel)
}