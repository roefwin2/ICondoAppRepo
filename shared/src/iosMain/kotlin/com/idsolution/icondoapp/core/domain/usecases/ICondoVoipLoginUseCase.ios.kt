package com.idsolution.icondoapp.core.domain.usecases

actual fun iCondoVoipLoginUseCase(
    username: String,
    password: String,
    domain: String
) {
    print("iCondoVoipLoginUseCase fro use case: $username $password $domain")
    VoipLogin.login(username, password, domain)
}