package com.idsolution.icondoapp.feature.voip.di

import com.idsolution.icondoapp.feature.voip.VoipViewModel
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetPhonebookUseCase
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val voipViewModelModule = module {
    viewModel {
        VoipViewModel(
            getPhonebookUseCase = get<GetPhonebookUseCase>(),
            voipService = get<VoipService>()
        )
    }
}
