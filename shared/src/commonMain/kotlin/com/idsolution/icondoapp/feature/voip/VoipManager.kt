package com.idsolution.icondoapp.feature.voip

/**
 * Interface pour la gestion VoIP multiplateforme
 */
interface VoipManager {
    fun logout()
    fun isConnected(): Boolean
}

/**
 * Implémentation par défaut utilisant VoipLogout singleton
 */
class VoipManagerImpl : VoipManager {

    override fun logout() {
        println("📴 VoipManagerImpl: Calling VoipLogout.logout()")
        VoipLogout.logout()
    }

    override fun isConnected(): Boolean {
        // L'état est géré côté plateforme
        return VoipLogout.isConfigured()
    }
}