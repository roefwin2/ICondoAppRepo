package com.idsolution.icondoapp.feature.mainscreen.di

import com.idsolution.icondoapp.feature.mainscreen.MainViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mainViewModelModule = module {
    viewModelOf(::MainViewModel)
}
