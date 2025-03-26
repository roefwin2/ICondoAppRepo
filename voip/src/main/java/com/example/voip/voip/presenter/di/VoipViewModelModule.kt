package com.example.voip.voip.presenter.di

import com.example.voip.voip.presenter.call.CallViewModel
import com.example.voip.voip.presenter.call.activities.VideoCallViewModel
import com.example.voip.voip.presenter.contacts.ContactsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val voipViewModelModule = module{
    viewModelOf(::ContactsViewModel)
    viewModelOf(::CallViewModel)
    viewModelOf(::VideoCallViewModel)
}