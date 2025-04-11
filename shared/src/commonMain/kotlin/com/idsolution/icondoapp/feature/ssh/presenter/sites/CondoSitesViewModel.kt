package com.example.testkmpapp.feature.ssh.presenter.sites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.example.testkmpapp.feature.ssh.domain.usecases.OpenDoorUseCase
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.presentation.helper.Error
import com.idsolution.icondoapp.core.presentation.helper.Loading
import com.idsolution.icondoapp.core.presentation.helper.Resource
import com.idsolution.icondoapp.core.presentation.helper.Success
import com.idsolution.icondoapp.feature.ssh.domain.usecases.GetSitesWithDoorsUseCase
import com.idsolution.icondoapp.feature.ssh.presenter.sites.CondoSitesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CondoSitesViewModel(
    private val openDoorUseCase: OpenDoorUseCase,
    private val getSitesWithDoorsUseCase: GetSitesWithDoorsUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<CondoSitesState> = MutableStateFlow(CondoSitesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val sites = getSitesWithDoorsUseCase.invoke()
            when (sites) {
                is Result.Success -> {
                    _state.update {
                        it.copy(sites = Success(sites.data))
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(sites = Error(errorCause = sites.message.toString()))
                    }
                }
            }
        }
    }

    fun onDoorChange(condoSite: CondoSite, doorNumber: Int, open: Boolean) {
        val loadingCondoSites = updateCondoSites(condoSite, doorNumber, Loading())
        _state.update {
            it.copy(sites = Success(loadingCondoSites))
        }
        viewModelScope.launch {
            val result = openDoorUseCase.invoke(condoSite, doorNumber)
            if (result is Result.Success) {
                val updatedCondoSites = updateCondoSites(condoSite, doorNumber, Success(open))
                _state.update {
                    it.copy(sites = Success(updatedCondoSites))
                }
            }
        }
    }

    private fun updateCondoSites(
        condoSite: CondoSite,
        doorNumber: Int,
        statusRes: Resource<Boolean>
    ): List<CondoSite> {
        val updatedCondoSite = updateDoorStatus(
            condoSite = condoSite,
            doorNumber = doorNumber,
            newStatus = statusRes
        )
        val currentStates = state.value.sites.value
        if (currentStates != null) {
            val updatedCondoSites = currentStates.map {
                if (condoSite.siteName == it.siteName) updatedCondoSite else it
            }
            return updatedCondoSites
        }
        return emptyList()
    }

    private fun updateDoorStatus(
        condoSite: CondoSite,
        doorNumber: Int,
        newStatus: Resource<Boolean>
    ): CondoSite {
        // Trouver et modifier la porte cible
        val updatedDoors = condoSite.doors.map { door ->
            if (door.number == doorNumber) door.copy(isOpen = newStatus) else door
        }.toSet()

        // Retourner un nouvel objet CondoSite avec les portes mises à jour
        return condoSite.copy(doors = updatedDoors)
    }
}