package com.idsolution.icondoapp.feature.voip

/**
 * Interface pour le callback de logout VoIP
 * Implémenté côté plateforme (iOS/Android)
 */
interface VoipLogoutListener {
    /**
     * Appelé quand le logout VoIP doit être effectué
     */
    fun onLogout()
}