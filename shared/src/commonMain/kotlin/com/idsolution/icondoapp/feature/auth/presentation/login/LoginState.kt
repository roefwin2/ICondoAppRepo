package com.example.testkmpapp.feature.auth.presentation.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import com.example.testkmpapp.feature.ssh.data.models.SiteDto

data class LoginState(
    val email: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val canLogin: Boolean = true,
    val isLoggingIn: Boolean = false,
    val doors: List<SiteDto> = emptyList()
)