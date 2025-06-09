package org.arcade.atomcity.di.viewmodel

import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.domain.usecase.GetMaiteaDataUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val maiTeaViewModelModule = module {
    single { MaiteaRepository(get()) }
    single { GetMaiteaDataUseCase(get()) }
    viewModelOf(::MaiteaViewModel)
}