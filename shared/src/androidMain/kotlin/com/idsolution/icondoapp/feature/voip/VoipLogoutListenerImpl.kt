package com.idsolution.icondoapp.feature.voip

import com.example.voip.voip.data.ICondoLinphoneImpl

class VoipLogoutListenerImpl(
    private var linphoneImpl: ICondoLinphoneImpl
) : VoipLogoutListener {

    override fun onLogout() {
        println("📴 VoipLogoutListenerImpl [Android]: onLogout called")
        try {
            linphoneImpl.logout()
            println("✅ VoipLogoutListenerImpl [Android]: VoIP logout successful")
        } catch (e: Exception) {
            println("❌ VoipLogoutListenerImpl [Android]: VoIP logout error: ${e.message}")
        }
    }
}