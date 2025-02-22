package org.arcade.atomcity.di

import org.arcade.atomcity.data.MaiTeaRepository
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    single { GetMaiTeaDataUseCase(get()) }
    single { MaiTeaRepository(get()) }
    viewModelOf(::MainActivityViewModel)
}