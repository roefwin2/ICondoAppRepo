package com.example.testkmpapp.di

import com.idsolution.icondoapp.feature.voip.VoipManager
import com.idsolution.icondoapp.feature.voip.VoipManagerImpl
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module{
        single<HttpClientEngine> { Darwin.create()  }
    }
actual val voipModule: Module
    get() = module {
        single<VoipManager> { VoipManagerImpl() }
    }