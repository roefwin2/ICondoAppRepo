//
//  VoIPPushManager.swift
//  iosApp
//
//  Gestionnaire des notifications push VoIP avec integration Linphone
//

import Foundation
import PushKit
import CallKit
import AVFoundation
import UIKit

/// Gestionnaire des notifications push VoIP via PushKit
final class VoIPPushManager: NSObject {

    // MARK: - Singleton

    static let shared = VoIPPushManager()

    // MARK: - Properties

    private let pushRegistry: PKPushRegistry
    private var currentUserID: String?
    private var currentPhoneNumber: String?

    // MARK: - Initialization

    private override init() {
        pushRegistry = PKPushRegistry(queue: nil)

        super.init()

        pushRegistry.delegate = self
        pushRegistry.desiredPushTypes = [.voIP]

        print("[VoIPPush] Initialized")
    }

    // MARK: - Public Methods

    /// Charge les informations utilisateur depuis UserDefaults
    func loadUserInfo() {
        currentPhoneNumber = UserDefaults.standard.string(forKey: "currentPhoneNumber")
        currentUserID = UserDefaults.standard.string(forKey: "currentUserID")

        if let phone = currentPhoneNumber, let userID = currentUserID {
            print("[VoIPPush] User info loaded: \(phone) -> \(userID)")
        } else {
            print("[VoIPPush] No saved user info")
        }
    }

    /// Verifie si l'utilisateur est enregistre
    func isUserRegistered() -> Bool {
        return currentPhoneNumber != nil && currentUserID != nil
    }

    /// Retourne le numero de telephone actuel
    func getCurrentPhoneNumber() -> String? {
        return currentPhoneNumber
    }

    /// Retourne l'ID utilisateur actuel
    func getCurrentUserID() -> String? {
        return currentUserID
    }

    /// Enregistre l'utilisateur
    func registerUser(phoneNumber: String, userID: String) {
        currentPhoneNumber = phoneNumber
        currentUserID = userID

        UserDefaults.standard.set(phoneNumber, forKey: "currentPhoneNumber")
        UserDefaults.standard.set(userID, forKey: "currentUserID")

        print("[VoIPPush] User registered: \(phoneNumber) -> \(userID)")

        // Re-register VoIP token if available
        if let token = UserDefaults.standard.string(forKey: "voipToken") {
            registerTokenWithLinphone(token: token)
        }
    }

    // MARK: - Private Methods

    private func registerTokenWithLinphone(token: String) {
        let manager = LinphoneManager.shared

        manager.core.didRegisterForRemotePushWithStringifiedToken(deviceTokenStr: token)
        manager.core.refreshRegisters()

        print("[VoIPPush] Token registered with Linphone: \(token)")
    }

    private func ensureLinphoneInitialized() {
        let manager = LinphoneManager.shared

        if manager.core == nil {
            print("[VoIPPush] Linphone Core not initialized!")
            return
        }

        manager.core.ensureRegistered()
        manager.core.refreshRegisters()

        print("[VoIPPush] Linphone Core ensured initialized")
    }
}

// MARK: - PKPushRegistryDelegate

extension VoIPPushManager: PKPushRegistryDelegate {

    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        let tokenParts = pushCredentials.token.map { String(format: "%02.2hhx", $0) }
        let token = tokenParts.joined()
        let voipToken = token + ":voip"

        print("[VoIPPush] VoIP token received: \(voipToken)")

        // Save token
        UserDefaults.standard.set(voipToken, forKey: "voipToken")

        // Register with Linphone
        registerTokenWithLinphone(token: voipToken)
    }

    func pushRegistry(
        _ registry: PKPushRegistry,
        didReceiveIncomingPushWith payload: PKPushPayload,
        for type: PKPushType,
        completion: @escaping () -> Void
    ) {
        guard type == .voIP else {
            completion()
            return
        }

        let payloadDict = payload.dictionaryPayload
        print("[VoIPPush] Push received: \(payloadDict)")

        // Extract caller info (support multiple field names)
        let displayName = (payloadDict["caller"] as? String)
            ?? (payloadDict["display-name"] as? String)
            ?? (payloadDict["sip-from"] as? String)
            ?? (payloadDict["from-uri"] as? String)
            ?? "Unknown"

        let callId = (payloadDict["uuid"] as? String)
            ?? (payloadDict["call-id"] as? String)
            ?? UUID().uuidString

        print("[VoIPPush] Incoming call from: \(displayName) (callId: \(callId))")

        // Ensure Linphone is ready
        ensureLinphoneInitialized()

        // Mark incoming call
        DispatchQueue.main.async {
            LinphoneManager.shared.isIncomingCall
        }

        completion()
    }
}

// MARK: - Notification Names

extension Notification.Name {
    static let voipCallAnswered = Notification.Name("voipCallAnswered")
    static let voipCallRejected = Notification.Name("voipCallRejected")
    static let voipCallEnded = Notification.Name("voipCallEnded")
    static let voipCallConnected = Notification.Name("voipCallConnected")
}
