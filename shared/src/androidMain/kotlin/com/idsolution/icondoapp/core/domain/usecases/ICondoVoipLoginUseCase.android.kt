package com.idsolution.icondoapp.core.domain.usecases

import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.domain.VoipTransportType
import org.koin.java.KoinJavaComponent.inject

actual fun iCondoVoipLoginUseCase(
    username: String,
    password: String,
    domain: String
) {
    val voipService: VoipService by inject(VoipService::class.java)
    voipService.login(username, password, domain, VoipTransportType.UDP)
}
