package com.idsolution.icondoapp.feature.ssh.presenter.di

import com.idsolution.icondoapp.feature.ssh.presenter.sites.CondoSitesViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val sshViewModelModule = module {
    viewModelOf(::CondoSitesViewModel)
}