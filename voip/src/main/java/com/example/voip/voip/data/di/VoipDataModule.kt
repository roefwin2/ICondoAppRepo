package com.example.voip.voip.data.di

import com.example.voip.voip.data.ICondoLinphoneImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.example.voip.voip.domain.ICondoVoip

val voipDataModule = module {
    singleOf(::ICondoLinphoneImpl).bind<ICondoVoip>()
}