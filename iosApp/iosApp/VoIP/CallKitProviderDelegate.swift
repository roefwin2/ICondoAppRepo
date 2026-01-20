//
//  CallKitProviderDelegate.swift
//  iosApp
//
//  Delegate pour l'integration CallKit avec support video complet
//

import Foundation
import CallKit
import linphonesw
import AVFoundation

/// Delegate gerant l'integration avec CallKit pour les appels VoIP
final class CallKitProviderDelegate: NSObject {

    // MARK: - Properties

    private let provider: CXProvider
    private let callController = CXCallController()
    private weak var manager: LinphoneManager?
    private var currentCallUUID: UUID?

    // MARK: - Initialization

    init(manager: LinphoneManager) {
        self.manager = manager

        let configuration = CXProviderConfiguration()
        configuration.supportsVideo = true
        configuration.supportedHandleTypes = [.generic, .phoneNumber, .emailAddress]
        configuration.maximumCallsPerCallGroup = 1
        configuration.maximumCallGroups = 1

        provider = CXProvider(configuration: configuration)

        super.init()

        provider.setDelegate(self, queue: nil)

        print("[CallKitDelegate] Initialized")
    }

    // MARK: - Public Methods

    /// Signale un appel entrant a CallKit
    func reportIncomingCall() {
        currentCallUUID = UUID()

        let update = CXCallUpdate()
        let callerName = manager?.remoteAddress.isEmpty == false ?
            manager!.remoteAddress : manager?.defaultCallerName ?? "Incoming Call"

        update.remoteHandle = CXHandle(type: .generic, value: callerName)
        update.hasVideo = true

        provider.reportNewIncomingCall(with: currentCallUUID!, update: update) { error in
            if let error = error {
                print("[CallKitDelegate] Failed to report incoming call: \(error)")
            } else {
                print("[CallKitDelegate] Incoming call reported successfully")
            }
        }
    }

    /// Termine l'appel via CallKit
    func endCall() {
        guard let uuid = currentCallUUID else { return }

        let action = CXEndCallAction(call: uuid)
        let transaction = CXTransaction(action: action)

        callController.request(transaction) { error in
            if let error = error {
                print("[CallKitDelegate] Failed to end call: \(error)")
            } else {
                print("[CallKitDelegate] Call ended via CallKit")
            }
        }
    }

    /// Signale un appel sortant a CallKit
    func reportOutgoingCall(to address: String) {
        currentCallUUID = UUID()

        let handle = CXHandle(type: .generic, value: address)
        let action = CXStartCallAction(call: currentCallUUID!, handle: handle)
        action.isVideo = true

        let transaction = CXTransaction(action: action)

        callController.request(transaction) { error in
            if let error = error {
                print("[CallKitDelegate] Failed to report outgoing call: \(error)")
            } else {
                print("[CallKitDelegate] Outgoing call reported")
            }
        }
    }
}

// MARK: - CXProviderDelegate

extension CallKitProviderDelegate: CXProviderDelegate {

    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        print("[CallKitDelegate] Answer call action")

        guard let manager = manager else {
            action.fail()
            return
        }

        // Configure audio session
        manager.core.configureAudioSession()

        // Accept the call with video
        manager.acceptCall()

        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        print("[CallKitDelegate] End call action")

        guard let manager = manager else {
            action.fulfill()
            return
        }

        // Terminate the call
        do {
            if let call = manager.activeCall ?? manager.core.currentCall {
                if call.state != .End && call.state != .Released {
                    try call.terminate()
                }
            }
        } catch {
            print("[CallKitDelegate] Failed to terminate call: \(error)")
        }

        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        print("[CallKitDelegate] Start call action")
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        print("[CallKitDelegate] Set held call action")
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        print("[CallKitDelegate] Set muted call action: \(action.isMuted)")

        manager?.core.micEnabled = !action.isMuted
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXPlayDTMFCallAction) {
        print("[CallKitDelegate] Play DTMF action")
        action.fulfill()
    }

    func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        print("[CallKitDelegate] Action timed out: \(action)")
    }

    func providerDidReset(_ provider: CXProvider) {
        print("[CallKitDelegate] Provider did reset")
    }

    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        print("[CallKitDelegate] Audio session activated")
        manager?.core.activateAudioSession(actived: true)
    }

    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        print("[CallKitDelegate] Audio session deactivated")
        manager?.core.activateAudioSession(actived: false)
    }
}
