package com.example.voip.voip.core.di

import com.example.voip.voip.core.notification.CallService
import com.example.voip.voip.core.service.CoreKeepAliveThirdPartyAccountsService
import com.example.voip.voip.data.ICondoLinphoneImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.example.voip.voip.domain.ICondoVoip

val coreModule = module {
    single {
        CoreKeepAliveThirdPartyAccountsService(get())
    }
}