package com.idsolution.icondoapp.feature.auth.presentation.di

import com.example.testkmpapp.feature.auth.presentation.login.LoginViewModel
import com.idsolution.icondoapp.feature.auth.presentation.createuser.SignupViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
}