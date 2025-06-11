package com.example.testkmpapp.feature.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import com.idsolution.icondoapp.feature.ssh.data.models.sites.SiteDto

data class LoginState(
    val email: TextFieldState = TextFieldState("demo@fitz.com"),
    val password: TextFieldState = TextFieldState("demoFitz1!"),
    val voipUsername: TextFieldState = TextFieldState("+33651690406"),
    val voipPassword: TextFieldState = TextFieldState("e1d2o3U4"),
    val voipDomain: TextFieldState = TextFieldState("sip.linphone.org"),
    val isPasswordVisible: Boolean = false,
    val isVoipPasswordVisible: Boolean = false,
    val canLogin: Boolean = true,
    val isLoggingIn: Boolean = false,
    val doors: List<SiteDto> = emptyList()
)
