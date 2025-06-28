package com.example.testkmpapp.feature.ssh.domain

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.idsolution.icondoapp.feature.ssh.data.models.phonebook.PhoneBookDtoItem
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorName
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorStatus
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import kotlinx.coroutines.flow.Flow

interface CondoSSHRepository {
    suspend fun domains(): Result<List<CondoSite>, DataError.Network>
    suspend fun phonebook(siteName: String): Result<List<PhoneBook>, DataError.Network>
    suspend fun startTunnel(
        hostname: String,
        localPort: Int,
        username: String,
        password: String,
        sshPort: Int,
        siteName: String
    ): EmptyDataResult<DataError.Network>

    suspend fun submitLogin(
        username: String,
        password: String,
        siteName: String
    ): EmptyDataResult<DataError.Network>

    suspend fun unlockDoor(
        doorId: Int,
        siteName: String
    ): Result<String, DataError.Network>

    fun getDoorStatus(siteName: String): Flow<Result<List<DoorStatus>, DataError.Network>>
    suspend fun getDoorsName(siteName: String): Result<List<DoorName>, DataError.Network>

    suspend fun getCamera(siteId: String): Result<String, DataError.Network>
}