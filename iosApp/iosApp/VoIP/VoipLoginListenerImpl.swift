//
//  VoipLoginListenerImpl.swift
//  iosApp
//
//  Implementation du listener VoIP pour le bridge Kotlin
//

import Foundation
import shared

/// Implementation du listener pour recevoir les credentials VoIP depuis Kotlin
final class VoipLoginListenerImpl: VoipLoginListener {

    // MARK: - Initialization

    init() {
        print("[VoipLoginListener] Initialized")
    }

    // MARK: - VoipLoginListener

    func onEvent(username: String, password: String, domain: String) {
        print("[VoipLoginListener] Login event received: \(username)@\(domain)")

        DispatchQueue.main.async {
            LinphoneManager.shared.login(
                username: username,
                password: password,
                domain: domain
            )
        }
    }
}
