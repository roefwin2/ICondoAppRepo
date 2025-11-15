package com.example.testkmpapp.di

import com.example.testkmpapp.core.data.auth.EncryptedSessionsStorage
import com.example.testkmpapp.core.data.networking.createHttpClient
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module
val shareModule = module {
    single {
        EncryptedSessionsStorage(get())
    }
    single {
        createHttpClient(get(), get())
    }
    single {
        AuthSessionManager(get(), get())
    }
}
expect val voipModule: Module