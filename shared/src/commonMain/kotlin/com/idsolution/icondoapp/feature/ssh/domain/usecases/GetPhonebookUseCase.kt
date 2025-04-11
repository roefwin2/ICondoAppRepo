package com.idsolution.icondoapp.feature.ssh.domain.usecases

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

class GetPhonebookUseCase(
    private val authRepository: AuthRepository,
    private val condoSSHRepository: CondoSSHRepository
) {

    suspend fun invoke(): Result<List<PhoneBook>, DataError.Network> {
        authRepository.loggedUser?.siteName ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        val result = condoSSHRepository.domains()
        if (result is Result.Error) return Result.Error(DataError.Network.SERVER_ERROR)
        if (result is Result.Success) {
            val site = result.data.firstOrNull { it.siteName == authRepository.loggedUser?.siteName }
                val res = site?.siteName?.let { condoSSHRepository.phonebook(siteName = it) } ?: Result.Error(DataError.Network.UNKNOWN)
                return res
            }else{
                return Result.Error(DataError.Network.SERVER_ERROR)
            }
        }

}