package com.idsolution.icondoapp.feature.voip.di

import com.idsolution.icondoapp.feature.voip.VoipViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val voipViewModelModule = module {
    viewModelOf(::VoipViewModel)
}