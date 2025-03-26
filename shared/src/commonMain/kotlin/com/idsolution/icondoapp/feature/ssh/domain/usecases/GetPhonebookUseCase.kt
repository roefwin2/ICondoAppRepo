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
        val siteName = authRepository.loggedUser?.siteName ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        val phoneBook = condoSSHRepository.phonebook(siteName)
       return phoneBook
    }
}