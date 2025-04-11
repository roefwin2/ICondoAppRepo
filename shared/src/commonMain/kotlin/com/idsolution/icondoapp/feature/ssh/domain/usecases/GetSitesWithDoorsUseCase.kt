package com.idsolution.icondoapp.feature.ssh.domain.usecases

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.example.testkmpapp.feature.ssh.domain.usecases.StartTunnelUseCase
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

class GetSitesWithDoorsUseCase(
    private val authRepository: AuthRepository,
    private val condoSSHRepository: CondoSSHRepository,
    private val startTunnelUseCase: StartTunnelUseCase
) {

    suspend fun invoke(): Result<List<CondoSite>, DataError.Network> {
        val currentUser =
            authRepository.loggedUser ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        val result = condoSSHRepository.domains()
        if (result is Result.Error) return result
        if (result is Result.Success) {
            val sites = result.data

            // Trouver le site actuel de l'utilisateur
            val userSite = sites.find { it.siteName == currentUser.siteName }
            if (userSite != null) {
                // Si le site actuel existe, lancer le tunnel SSH
                val startTunnelResult = startTunnelUseCase.invoke(userSite)
                if (startTunnelResult is Result.Error) return startTunnelResult
            }
            // Mettre à jour les portes du site actuel de l'utilisateur
            val sitesWithDoors = sites.map { site ->
                if (site.siteName == currentUser.siteName) {
                    site.copy(doors = currentUser.accessDoors)
                } else {
                    site
                }
            }
            return Result.Success(sitesWithDoors)
        }
        return Result.Error(DataError.Network.SERVER_ERROR)
    }

}