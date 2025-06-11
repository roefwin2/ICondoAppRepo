package com.idsolution.icondoapp.core.domain.usecases

expect fun iCondoVoipLoginUseCase(username: String, password: String, domain: String)

interface VoipLoginListener {
    fun onEvent(username: String, password: String, domain: String)
}

object VoipLogin {
    var voipLoginListener: VoipLoginListener? = null

    fun setListener(listener: VoipLoginListener) {
        this.voipLoginListener = listener
    }

    fun login(username: String, password: String, domain: String) {
        voipLoginListener?.onEvent(username, password, domain)
    }
}