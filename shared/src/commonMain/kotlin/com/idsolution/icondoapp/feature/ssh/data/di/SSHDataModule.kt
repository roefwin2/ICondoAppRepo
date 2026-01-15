package com.idsolution.icondoapp.feature.ssh.data.di

import com.idsolution.icondoapp.feature.ssh.data.CondoSSHRepositoryImpl
import com.idsolution.icondoapp.feature.ssh.domain.CondoSSHRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sshDataModule = module {
    singleOf(::CondoSSHRepositoryImpl).bind<CondoSSHRepository>()
}