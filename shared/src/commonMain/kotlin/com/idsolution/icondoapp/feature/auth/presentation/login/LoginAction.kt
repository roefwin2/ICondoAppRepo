package com.example.testkmpapp.feature.auth.presentation.login

sealed interface LoginAction {
    data object OnTogglePasswordVisibility: LoginAction
    data object OnLoginClick: LoginAction
    data  class OnLoginChanged(val email: String,val password: String): LoginAction
    data object OnRegisterClick: LoginAction
}