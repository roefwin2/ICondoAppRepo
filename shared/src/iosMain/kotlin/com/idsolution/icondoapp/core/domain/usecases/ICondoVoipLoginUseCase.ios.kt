package com.idsolution.icondoapp.core.domain.usecases

actual fun iCondoVoipLoginUseCase(
    username: String,
    password: String,
    domain: String
) {
    VoipLogin.login(username, password, domain)
}