package com.example.testkmpapp.di

import com.example.voip.voip.data.ICondoLinphoneImpl
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.presenter.call.CallViewModel
import com.example.voip.voip.presenter.call.activities.VideoCallViewModel
import com.example.voip.voip.presenter.contacts.ContactsViewModel
import com.idsolution.icondoapp.feature.voip.VoipViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { OkHttp.create() }
    }

actual val voipModule: Module
    get() = module {
        viewModelOf(::ContactsViewModel)
        viewModelOf(::CallViewModel)
        viewModelOf(::VideoCallViewModel)
        singleOf(::ICondoLinphoneImpl).bind<ICondoVoip>()
    }