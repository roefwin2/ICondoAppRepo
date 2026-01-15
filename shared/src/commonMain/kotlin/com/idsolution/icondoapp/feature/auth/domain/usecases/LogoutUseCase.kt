package com.idsolution.icondoapp.feature.auth.domain.usecases

import com.idsolution.icondoapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import com.idsolution.icondoapp.feature.voip.VoipLogout

/**
 * UseCase pour gérer la déconnexion complète de l'utilisateur
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val authSessionManager: AuthSessionManager
) {
    suspend operator fun invoke(): LogoutResult {
        return try {
            println("🚪 LogoutUseCase: Starting logout process...")

            // Étape 1: Révocation du token backend
            println("📡 LogoutUseCase: Step 1 - Revoking backend token")
            val backendResult = authRepository.revokeToken()
            val backendSuccess = backendResult is Result.Success

            if (!backendSuccess) {
                println("⚠️ LogoutUseCase: Backend token revocation failed, continuing...")
            } else {
                println("✅ LogoutUseCase: Backend token revoked")
            }

            // Étape 2: Déconnexion VoIP via le singleton
            println("📴 LogoutUseCase: Step 2 - VoIP logout")
            try {
                VoipLogout.logout()
                println("✅ LogoutUseCase: VoIP logout triggered")
            } catch (e: Exception) {
                println("⚠️ LogoutUseCase: VoIP logout failed: ${e.message}")
            }

            // Étape 3: Nettoyage session locale
            println("🧹 LogoutUseCase: Step 3 - Clearing local session")
            authSessionManager.logout()

            println("✅ LogoutUseCase: Logout complete!")
            LogoutResult.Success

        } catch (e: Exception) {
            println("❌ LogoutUseCase: Logout failed: ${e.message}")

            // Forcer le logout local même en cas d'erreur
            try { VoipLogout.logout() } catch (e: Exception) {
                println("⚠️ LogoutUseCase: Failed to force VoIP logout : ${e.message}")
            }
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