package com.example.testkmpapp.feature.auth.data.di

import com.example.testkmpapp.feature.auth.data.AuthRepositoryImpl
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
}