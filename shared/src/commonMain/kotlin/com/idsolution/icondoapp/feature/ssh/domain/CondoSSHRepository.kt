package com.example.testkmpapp.feature.ssh.domain

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite

interface CondoSSHRepository {
    suspend fun domains(): Result<List<CondoSite>, DataError.Network>
    suspend fun startTunnel(
        hostname: String,
        localPort: Int,
        username: String,
        password: String,
        siteName: String
    ): EmptyDataResult<DataError.Network>

    suspend fun submitLogin(
        username: String,
        password: String,
        siteName: String
    ): EmptyDataResult<DataError.Network>

    suspend fun unlockDoor(
        lobbyDoor: Int,
        siteName: String
    ): Result<String, DataError.Network>

    suspend fun getCamera(siteId: String): Result<String, DataError.Network>
}