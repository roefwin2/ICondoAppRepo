package com.idsolution.icondoapp.di

import com.idsolution.icondoapp.core.data.auth.EncryptedSessionsStorage
import com.idsolution.icondoapp.core.data.networking.createHttpClient
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific module for HTTP client engine and other platform dependencies
 */
expect val platformModule: Module

/**
 * Platform-specific VoIP module
 */
expect val voipModule: Module

/**
 * Shared module - common dependencies
 */
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
