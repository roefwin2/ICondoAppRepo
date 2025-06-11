package com.idsolution.icondoapp.feature.auth.presentation.createuser

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.domain.usecases.ICondoRegisterUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val iCondoRegisterUseCase: ICondoRegisterUseCase
) : ViewModel() {

    var state by mutableStateOf(SignupState())
        private set

    private val _events = Channel<SignupEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SignupAction) {
        when (action) {
            is SignupAction.OnFirstNameChange -> {
                state = state.copy(firstName = TextFieldState(action.value))
                validateForm()
            }

            is SignupAction.OnLastNameChange -> {
                state = state.copy(lastName = TextFieldState(action.value))
                validateForm()
            }

            is SignupAction.OnEmailChange -> {
                state = state.copy(email = TextFieldState(action.value))
                validateEmail()
                validateForm()
            }

            is SignupAction.OnPasswordChange -> {
                state = state.copy(password = TextFieldState(action.value))
                validateForm()
            }

            SignupAction.OnTogglePasswordVisibility -> {
                state = state.copy(isPasswordVisible = !state.isPasswordVisible)
            }
            is SignupAction.OnVoipUsernameChange -> {
                state = state.copy(voipUsername = TextFieldState(action.value))
                validateForm()
            }
            is SignupAction.OnVoipPasswordChange -> {
                state = state.copy(voipPassword = TextFieldState(action.value))
                validateForm()
            }

            is SignupAction.OnVoipDomainChange -> {
                state = state.copy(voipDomain = TextFieldState(action.value))
                validateForm()
            }
            SignupAction.OnToggleVoipPasswordVisibility -> {
                state = state.copy(isVoipPasswordVisible = !state.isVoipPasswordVisible)
            }

            SignupAction.OnSignupClick -> {
                signup()
            }

            SignupAction.OnLoginClick -> {
                // Handled in SignupScreenRoot
            }
        }
    }

    private fun validateEmail() {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val email = state.email.text
        if (email.isEmpty()) {
            state = state.copy(emailError = null)
        } else if (!email.matches(emailPattern.toRegex())) {
            state = state.copy(emailError = "Invalid email format")
        } else {
            state = state.copy(emailError = null)
        }
    }

    private fun validateForm() {
        state = state.copy(
            canSignup = true
        )
    }

    private fun signup() {
        viewModelScope.launch {
            state = state.copy(isSigningUp = true)
            try {
                iCondoRegisterUseCase.invoke(
                    firstName = state.firstName.text.toString(),
                    lastName = state.lastName.text.toString(),
                    email = state.email.text.toString(),
                    password = state.password.text.toString(),
                    voipUsername = state.voipUsername.text.toString(),
                    voipPassword = state.voipPassword.text.toString(),
                    voipDomain = state.voipDomain.text.toString()
                ).collectLatest { result ->
                    if (result is Result.Success) {
                        _events.send(SignupEvent.SignupSuccess)
                    } else {
                        _events.send(
                            SignupEvent.Error(
                                "Unknown error"
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                _events.send(SignupEvent.Error(e.message ?: "Unknown error"))
            } finally {
                state = state.copy(isSigningUp = false)
            }
        }
    }
}