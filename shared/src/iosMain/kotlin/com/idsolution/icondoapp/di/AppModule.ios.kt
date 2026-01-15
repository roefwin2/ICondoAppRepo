package com.idsolution.icondoapp.di

import com.idsolution.icondoapp.feature.voip.VoipServiceFactory
import com.idsolution.icondoapp.feature.voip.domain.DefaultVoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific platform module
 */
actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { Darwin.create() }
    }

/**
 * iOS VoIP module using Linphone SDK via CocoaPods
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
