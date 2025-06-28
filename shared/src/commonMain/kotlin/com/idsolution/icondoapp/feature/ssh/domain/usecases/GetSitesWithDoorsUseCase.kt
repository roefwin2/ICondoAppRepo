package com.idsolution.icondoapp.feature.ssh.domain.usecases

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.example.testkmpapp.feature.ssh.domain.models.Door
import com.example.testkmpapp.feature.ssh.domain.usecases.StartTunnelUseCase
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorName
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorStatus
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class GetSitesWithDoorsUseCase(
    private val authRepository: AuthRepository,
    private val condoSSHRepository: CondoSSHRepository,
    private val startTunnelUseCase: StartTunnelUseCase
) {

    fun invoke(): Flow<Result<List<CondoSite>, DataError.Network>> = flow {
        val currentUser =
            authRepository.loggedUser
                ?: return@flow emit(Result.Error(DataError.Network.UNAUTHORIZED))
        val siteName =
            currentUser.siteName ?: return@flow emit(Result.Error(DataError.Network.UNAUTHORIZED))
        val doorsName = condoSSHRepository.getDoorsName(siteName)
        val accessDoorsWithName = if (doorsName is Result.Success) {
            updateDoorNames(currentUser.accessDoors.toList(), doorsName.data)
        } else {
            currentUser.accessDoors
        }

        val result = condoSSHRepository.domains()
        if (result is Result.Error) return@flow emit(result)
        if (result is Result.Success) {
            val sites = result.data

            // Trouver le site actuel de l'utilisateur
            val userSite = sites.find { it.siteName == currentUser.siteName }
            if (userSite != null) {
                // Si le site actuel existe, lancer le tunnel SSH
                val startTunnelResult = startTunnelUseCase.invoke(userSite)
                if (startTunnelResult is Result.Error) return@flow emit(startTunnelResult)
            }

            // Mettre à jour les portes du site actuel de l'utilisateur
            val sitesWithDoors = sites.map { site ->
                if (site.siteName == currentUser.siteName) {
                    site.copy(doors = accessDoorsWithName.toSet())
                } else {
                    site
                }
            }
            emit(Result.Success(sitesWithDoors))
        }
    }

    fun updateDoorNames(
        doors: List<Door>,
        doorNames: List<DoorName>
    ): List<Door> {
        // Crée une map pour un accès rapide : id -> doorName
        val doorNameMap = doorNames.associateBy { it.id }

        return doors.map { door ->
            val newName = doorNameMap[door.number]?.doorName ?: door.name
            door.copy(name = newName)
        }
    }
}