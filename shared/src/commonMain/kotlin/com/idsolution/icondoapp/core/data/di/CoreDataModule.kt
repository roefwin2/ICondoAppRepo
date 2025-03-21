package com.idsolution.icondoapp.core.data.di

import com.example.testkmpapp.core.data.auth.EncryptedSessionsStorage
import com.idsolution.icondoapp.core.data.datastore.createDataStore
import com.idsolution.icondoapp.core.domain.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::EncryptedSessionsStorage).bind<SessionStorage>()
    single {
        createDataStore()
    }
}