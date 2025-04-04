package com.idsolution.icondoapp.core.domain.usecases

import com.example.voip.voip.domain.ICondoVoip
import org.koin.java.KoinJavaComponent.inject
import org.linphone.core.TransportType

actual fun iCondoVoipLoginUseCase(
    username: String,
    password: String,
    domain: String
) {
    val iCondoVoip: ICondoVoip by inject(ICondoVoip::class.java)
    iCondoVoip.login(username, password, domain, transportType = TransportType.Tcp)
}