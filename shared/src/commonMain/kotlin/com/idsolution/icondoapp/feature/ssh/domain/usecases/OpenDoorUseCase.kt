package com.example.testkmpapp.feature.ssh.domain.usecases

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite

class OpenDoorUseCase(
    private val condoSSHRepository: CondoSSHRepository
) {

    suspend fun invoke(
        condoSite: CondoSite,
        door: Int
    ): Result<Unit, com.idsolution.icondoapp.core.data.networking.Error> {
        val startTunnel = condoSSHRepository.startTunnel(
            hostname = condoSite.host,
            localPort = condoSite.port,
            username = "root",
            password = "icondo",
            siteName = condoSite.siteName
        )
        if (startTunnel is Error) return Result.Error(
            DataError.Network.SERVER_ERROR
        )
        val submitLogin = condoSSHRepository.submitLogin(
            username = "admin",
            password = "1234",
            siteName = condoSite.siteName
        )
        if (submitLogin is Error) return Result.Error(
            DataError.Network.SERVER_ERROR
        )
        val result =
            condoSSHRepository.unlockDoor(doorId = door, siteName = condoSite.siteName)
        return if (result is Result.Success) {
            Result.Success(Unit)
        } else {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }
}