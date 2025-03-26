package com.example.testkmpapp.feature.ssh.domain.di

import com.example.testkmpapp.feature.ssh.domain.usecases.OpenDoorUseCase
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetPhonebookUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sshDomainModule = module {
    singleOf(::OpenDoorUseCase)
    singleOf(::GetPhonebookUseCase)
}