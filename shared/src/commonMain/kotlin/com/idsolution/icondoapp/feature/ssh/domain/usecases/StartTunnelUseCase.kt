package com.example.testkmpapp.feature.ssh.domain.usecases

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite

class StartTunnelUseCase(
    private val condoSSHRepository: CondoSSHRepository
) {
    suspend fun invoke(
        condoSite: CondoSite,
    ): Result<Unit, DataError.Network> {
        val startTunnel = condoSSHRepository.startTunnel(
            hostname = condoSite.host,
            localPort = condoSite.port,
            username = "root",
            password = "icondo",
            sshPort = condoSite.siteSshPort,
            siteName = condoSite.siteName,
            )
        if (startTunnel is Error) return startTunnel
        val submitLogin = condoSSHRepository.submitLogin(
            username = condoSite.siteUser,
            password = condoSite.sitePwd,
            siteName = condoSite.siteName
        )
        return submitLogin
    }
}