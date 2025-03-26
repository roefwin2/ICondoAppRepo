package com.example.testkmpapp.di

import com.example.testkmpapp.core.data.auth.EncryptedSessionsStorage
import com.idsolution.icondoapp.core.data.networking.HttpClientFactory
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module
val shareModule = module {
    single {
        EncryptedSessionsStorage(get())
    }
    single {
        HttpClientFactory(get()).build(get())
    }
    single {
        AuthSessionManager(get(),get())
    }
}
expect val voipModule: Module