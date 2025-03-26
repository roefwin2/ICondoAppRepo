package com.idsolution.icondoapp.feature.auth.presentation.createuser

import androidx.compose.foundation.text.input.TextFieldState

data class SignupState(
    val firstName: TextFieldState = TextFieldState(""),
    val lastName: TextFieldState = TextFieldState(""),
    val email: TextFieldState = TextFieldState(""),
    val password: TextFieldState = TextFieldState(""),
    val isPasswordVisible: Boolean = false,
    val isSigningUp: Boolean = false,
    val canSignup: Boolean = false,
    val emailError: String? = null
)

sealed class SignupAction {
    data class OnFirstNameChange(val value: String) : SignupAction()
    data class OnLastNameChange(val value: String) : SignupAction()
    data class OnEmailChange(val value: String) : SignupAction()
    data class OnPasswordChange(val value: String) : SignupAction()
    object OnTogglePasswordVisibility : SignupAction()
    object OnSignupClick : SignupAction()
    object OnLoginClick : SignupAction()
}

sealed class SignupEvent {
    data class Error(val message: String) : SignupEvent()
    object SignupSuccess : SignupEvent()
}