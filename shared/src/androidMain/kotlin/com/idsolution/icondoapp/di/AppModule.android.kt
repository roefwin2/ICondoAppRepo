package com.idsolution.icondoapp.di

import com.idsolution.icondoapp.feature.voip.VoipServiceFactory
import com.idsolution.icondoapp.feature.voip.VoipViewModel
import com.idsolution.icondoapp.feature.voip.domain.DefaultVoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific platform module
 */
actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { OkHttp.create() }
    }

/**
 * Android VoIP module using Linphone SDK
 */
actual val voipModule: Module
    get() = module {
        // VoIP Event Handler
        single<VoipEventHandler> { DefaultVoipEventHandler() }

        // VoIP Service - singleton using factory
        single<VoipService> {
            VoipServiceFactory.create(get())
        }
    }
