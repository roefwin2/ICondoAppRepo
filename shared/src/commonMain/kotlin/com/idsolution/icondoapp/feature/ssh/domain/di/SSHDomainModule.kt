package com.example.testkmpapp.feature.ssh.domain.di

import com.example.testkmpapp.feature.ssh.domain.usecases.OpenDoorUseCase
import com.example.testkmpapp.feature.ssh.domain.usecases.StartTunnelUseCase
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetPhonebookUseCase
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetSitesWithDoorsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sshDomainModule = module {
    singleOf(::OpenDoorUseCase)
    singleOf(::GetPhonebookUseCase)
    singleOf(::GetSitesWithDoorsUseCase)
    singleOf(::StartTunnelUseCase)
}