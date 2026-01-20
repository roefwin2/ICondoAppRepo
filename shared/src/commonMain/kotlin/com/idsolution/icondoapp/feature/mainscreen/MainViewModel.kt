package com.idsolution.icondoapp.feature.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idsolution.icondoapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.feature.auth.domain.usecases.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val username = authRepository.loggedUser?.username ?: ""
            _state.value = _state.value.copy(username = username)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = logoutUseCase.invoke()
            println("MainViewModel: Logout result: $result")
        }
    }
}

data class MainState(
    val username: String = "",
)