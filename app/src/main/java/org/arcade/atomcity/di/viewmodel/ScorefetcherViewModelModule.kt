package org.arcade.atomcity.di.viewmodel

import org.arcade.atomcity.data.repository.ScorefetcherRepository
import org.arcade.atomcity.domain.usecase.*
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val scorefetcherViewModelModule = module {
    viewModel {
        ScorefetcherViewModel(
            scoresUseCase = get(),
            profileUseCase = get(),
            importUseCase = get(),
            analyticsUseCase = get(),
            jacketUseCase = get()
        )
    }
}
