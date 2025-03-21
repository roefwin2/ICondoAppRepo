package com.idsolution.icondoapp.core.domain.di

import com.idsolution.icondoapp.core.domain.usecases.ICondoLoginUseCase
import org.koin.dsl.module

val coreDomainModule = module {
    single {
        ICondoLoginUseCase(get())
    }
}