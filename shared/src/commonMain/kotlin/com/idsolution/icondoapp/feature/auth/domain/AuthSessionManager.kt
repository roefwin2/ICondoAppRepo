package com.idsolution.icondoapp.feature.auth.domain

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gère l'état d'authentification de l'application
 */
class AuthSessionManager(
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Vérifie si l'utilisateur est connecté au démarrage de l'application
     */
    suspend fun checkAuthState() {
        val authInfo = sessionStorage.get()
        if (authInfo != null && authInfo.accessToken.isNotEmpty()) {
            // Récupérer les informations utilisateur depuis le serveur ou le stockage local
            val userName = authInfo.username
            val result = authRepository.getUser(userName)
            if (result is com.idsolution.icondoapp.core.data.networking.Result.Success) {
                _authState.value = AuthState.Authenticated(authRepository.loggedUser)
            } else {
                // La token existe mais on n'a pas pu récupérer l'utilisateur
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Mettre à jour l'état après connexion réussie
     */
    fun onLoginSuccess(user: ICondoUser?) {
        _authState.value = AuthState.Authenticated(user)
    }

    /**
     * Déconnexion de l'utilisateur
     */
    suspend fun logout() {
        sessionStorage.clear()
        _authState.value = AuthState.Unauthenticated
    }
}

/**
 * États possibles d'authentification
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: ICondoUser?) : AuthState()
}