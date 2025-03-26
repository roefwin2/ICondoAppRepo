package com.example.testkmpapp.di

import com.idsolution.icondoapp.core.data.di.coreDataModule
import com.idsolution.icondoapp.core.domain.di.coreDomainModule
import com.example.testkmpapp.feature.auth.data.di.authDataModule
import com.example.testkmpapp.feature.auth.presentation.di.authViewModelModule
import com.example.testkmpapp.feature.ssh.data.di.sshDataModule
import com.example.testkmpapp.feature.ssh.domain.di.sshDomainModule
import com.example.testkmpapp.feature.ssh.presenter.di.sshViewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
@Throws(Exception::class)
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            shareModule,
            authDataModule,
            coreDataModule,
            coreDomainModule,
            authViewModelModule,
            sshDataModule,
            sshDomainModule,
            sshViewModelModule,
            platformModule,
            voipModule
        )
    }
}