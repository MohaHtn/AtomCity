package org.arcade.atomcity.di.viewmodel

import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.domain.usecase.GetMaiteaDataUseCase
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val maiTeaViewModelModule = module {
    single { MaiteaRepository(get(), get(), get(), get()) }
    single { GetMaiteaDataUseCase(get()) }
    viewModel {
        MaiteaViewModel(
            repository = get(),
            jacketImages = get<Map<String, String>>(named("jacketImages"))
        )
    }
}
