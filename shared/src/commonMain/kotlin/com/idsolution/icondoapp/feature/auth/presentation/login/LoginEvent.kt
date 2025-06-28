package com.example.testkmpapp.feature.auth.presentation.login

import com.idsolution.icondoapp.core.presentation.helper.UiText

sealed interface LoginEvent {
    data object Idle: LoginEvent
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
}