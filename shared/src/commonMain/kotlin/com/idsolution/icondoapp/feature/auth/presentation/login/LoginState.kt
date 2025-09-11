package com.example.testkmpapp.feature.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import com.idsolution.icondoapp.feature.ssh.data.models.sites.SiteDto

data class LoginState(
    val email: TextFieldState = TextFieldState("demo@fitz.com"),
    val password: TextFieldState = TextFieldState("demoFitz1!"),
    val voipUsername: TextFieldState = TextFieldState("1001"),
    val voipPassword: TextFieldState = TextFieldState("idSolution123!!"),
    val voipDomain: TextFieldState = TextFieldState("31.97.155.55"),
    val isPasswordVisible: Boolean = false,
    val isVoipPasswordVisible: Boolean = false,
    val canLogin: Boolean = true,
    val isLoggingIn: Boolean = false,
    val doors: List<SiteDto> = emptyList()
)
