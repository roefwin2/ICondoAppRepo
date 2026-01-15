package com.idsolution.icondoapp.di

import com.idsolution.icondoapp.core.data.di.coreDataModule
import com.idsolution.icondoapp.core.domain.di.coreDomainModule
import com.idsolution.icondoapp.feature.auth.data.di.authDataModule
import com.idsolution.icondoapp.feature.auth.presentation.di.authViewModelModule
import com.idsolution.icondoapp.feature.mainscreen.di.mainViewModelModule
import com.idsolution.icondoapp.feature.ssh.data.di.sshDataModule
import com.idsolution.icondoapp.feature.ssh.domain.di.sshDomainModule
import com.idsolution.icondoapp.feature.ssh.presenter.di.sshViewModelModule
import com.idsolution.icondoapp.feature.rtsp.di.cameraModule
import com.idsolution.icondoapp.feature.voip.di.voipViewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Throws(Exception::class)
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            // Core modules
            shareModule,
            coreDataModule,
            coreDomainModule,
            platformModule,

            // Auth modules
            authDataModule,
            authViewModelModule,

            // SSH/Door modules
            sshDataModule,
            sshDomainModule,
            sshViewModelModule,

            // Camera/RTSP module
            cameraModule,

            // VoIP modules
            voipModule,
            voipViewModelModule,

            // Main screen
            mainViewModelModule
        )
    }
}
