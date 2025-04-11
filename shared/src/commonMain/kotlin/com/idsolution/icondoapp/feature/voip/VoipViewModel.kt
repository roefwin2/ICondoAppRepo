package com.idsolution.icondoapp.feature.voip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetPhonebookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoipViewModel(
    private val getPhonebookUseCase: GetPhonebookUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<Result<List<PhoneBook>, DataError.Network>> =
        MutableStateFlow(
            Result.Success(
                emptyList()
            )
        )
    val state = _state.asStateFlow()

    fun getPhonebook() {
        println("getPhonebook: function")
        viewModelScope.launch {
            val phoneBookResult = getPhonebookUseCase.invoke()
            println("getPhonebook: functio 2")
            _state.update {
                phoneBookResult
            }
        }
    }
}