package com.example.testkmpapp.feature.auth.presentation.di

import com.example.testkmpapp.feature.auth.presentation.login.LoginViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {
    viewModelOf(::LoginViewModel)
}