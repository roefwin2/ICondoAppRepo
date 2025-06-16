package com.example.testkmpapp.di

import com.bouyahya.kmpcall.di.appModule
import com.example.testkmpapp.feature.auth.data.di.authDataModule
import com.example.testkmpapp.feature.auth.presentation.di.mainViewModelModule
import com.example.testkmpapp.feature.ssh.data.di.sshDataModule
import com.example.testkmpapp.feature.ssh.domain.di.sshDomainModule
import com.example.testkmpapp.feature.ssh.presenter.di.sshViewModelModule
import com.idsolution.icondoapp.core.data.di.coreDataModule
import com.idsolution.icondoapp.core.domain.di.coreDomainModule
import com.idsolution.icondoapp.feature.auth.presentation.di.authViewModelModule
import com.idsolution.icondoapp.feature.voip.di.voipViewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Throws(Exception::class)
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            shareModule,
            authDataModule,
            coreDataModule,
            coreDomainModule,
            authViewModelModule,
            sshDataModule,
            sshDomainModule,
            sshViewModelModule,
            platformModule,
            voipModule,
            voipViewModelModule,
            mainViewModelModule
        )
    }
}