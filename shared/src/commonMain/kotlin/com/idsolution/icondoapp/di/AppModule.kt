package com.example.testkmpapp.di

import com.example.testkmpapp.core.data.auth.EncryptedSessionsStorage
import com.idsolution.icondoapp.core.data.networking.HttpClientFactory
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
}