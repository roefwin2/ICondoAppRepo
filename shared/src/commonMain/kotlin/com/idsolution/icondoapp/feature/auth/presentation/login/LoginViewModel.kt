package com.example.testkmpapp.feature.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.domain.usecases.ICondoLoginUseCase
import com.idsolution.icondoapp.core.presentation.helper.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val iCondoLoginUseCase: ICondoLoginUseCase
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility -> {
                state = state.copy(
                    isPasswordVisible = !state.isPasswordVisible
                )
            }
            is LoginAction.OnLoginChanged -> {
                state = state.copy(
                    email = TextFieldState(action.email),
                    password = TextFieldState(action.password)
                )
            }
            else -> Unit
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoggingIn = true)
            iCondoLoginUseCase.invoke(
                email = state.email.text.toString().trim(),
                password = state.password.text.toString(),
            ).collect { result ->
                state = state.copy(isLoggingIn = false)
                when (result) {
                    is Result.Error -> {
                        if (result.error == DataError.Network.UNAUTHORIZED) {
                            eventChannel.send(
                                LoginEvent.Error(
                                    UiText.DynamicString("Incorrect")
                                )
                            )
                        } else {
                            eventChannel.send(LoginEvent.Error(UiText.DynamicString("Error")))
                        }
                    }

                    is Result.Success -> {
                        eventChannel.send(LoginEvent.LoginSuccess)
                    }
                }
            }
        }
    }
}