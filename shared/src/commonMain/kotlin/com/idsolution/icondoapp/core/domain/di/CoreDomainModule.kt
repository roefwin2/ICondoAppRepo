package com.idsolution.icondoapp.core.domain.di

import com.idsolution.icondoapp.core.domain.usecases.ICondoLoginUseCase
import com.idsolution.icondoapp.core.domain.usecases.ICondoRegisterUseCase
import org.koin.dsl.module

val coreDomainModule = module {
    single {
        ICondoLoginUseCase(get(),get(),get())
    }
    single {
        ICondoRegisterUseCase(get(),get())
    }
}