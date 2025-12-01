// shared/src/commonMain/kotlin/com/idsolution/icondoapp/feature/voip/VoipLogout.kt

package com.idsolution.icondoapp.feature.voip

/**
 * Singleton pour gérer le logout VoIP de manière multiplateforme
 * Similaire à VoipLogin
 */
object VoipLogout {
    private var listener: VoipLogoutListener? = null

    /**
     * Configure le listener de logout
     * Doit être appelé au démarrage de l'app (iOS et Android)
     */
    fun setListener(listener: VoipLogoutListener) {
        this.listener = listener
        println("✅ VoipLogout: Listener configured")
    }

    /**
     * Déclenche le logout VoIP
     * Appelé par LogoutUseCase
     */
    fun logout() {
        println("📴 VoipLogout: Triggering logout")
        listener?.onLogout() ?: println("⚠️ VoipLogout: No listener configured")
    }

    /**
     * Vérifie si le listener est configuré
     */
    fun isConfigured(): Boolean = listener != null

    /**
     * Supprime le listener (utile pour les tests ou reset)
     */
    fun clearListener() {
        listener = null
        println("🧹 VoipLogout: Listener cleared")
    }
}