package com.idsolution.icondoapp.feature.auth.domain.usecases

import com.idsolution.icondoapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import com.idsolution.icondoapp.feature.voip.domain.VoipService

/**
 * UseCase for handling complete user logout
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val authSessionManager: AuthSessionManager,
    private val voipService: VoipService
) {
    suspend operator fun invoke(): LogoutResult {
        return try {
            println("LogoutUseCase: Starting logout process...")

            // Step 1: Revoke backend token
            println("LogoutUseCase: Step 1 - Revoking backend token")
            val backendResult = authRepository.revokeToken()
            val backendSuccess = backendResult is Result.Success

            if (!backendSuccess) {
                println("LogoutUseCase: Backend token revocation failed, continuing...")
            } else {
                println("LogoutUseCase: Backend token revoked")
            }

            // Step 2: VoIP logout via service
            println("LogoutUseCase: Step 2 - VoIP logout")
            try {
                voipService.logout()
                println("LogoutUseCase: VoIP logout completed")
            } catch (e: Exception) {
                println("LogoutUseCase: VoIP logout failed: ${e.message}")
            }

            // Step 3: Clear local session
            println("LogoutUseCase: Step 3 - Clearing local session")
            authSessionManager.logout()

            println("LogoutUseCase: Logout complete!")
            LogoutResult.Success

        } catch (e: Exception) {
            println("LogoutUseCase: Logout failed: ${e.message}")

            // Force local logout even on error
            try { voipService.logout() } catch (_: Exception) {}
            authSessionManager.logout()

            LogoutResult.PartialSuccess(e.message ?: "Unknown error")
        }
    }
}

sealed class LogoutResult {
    object Success : LogoutResult()
    data class PartialSuccess(val warning: String) : LogoutResult()
    data class Failure(val error: String) : LogoutResult()
}
