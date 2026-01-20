//
//  LinphoneManager.swift
//  iosApp
//
//  Gestionnaire principal de Linphone avec support CallKit et video
//  Refactored from CallKitExampleContext following best practices
//

import Foundation
import SwiftUI
import linphonesw
import Combine
import AVFoundation

/// Gestionnaire principal pour les appels VoIP via Linphone SDK
/// Gere la connexion SIP, les appels entrants/sortants, et l'integration CallKit
final class LinphoneManager: ObservableObject {

    // MARK: - Singleton

    static let shared = LinphoneManager()

    // MARK: - Linphone Core

    private(set) var core: Core!
    @Published private(set) var coreVersion: String = Core.getVersion

    // MARK: - Account

    private var account: Account?
    private var coreDelegate: CoreDelegate!

    @Published var username: String = ""
    @Published var password: String = ""
    @Published var domain: String = ""
    @Published private(set) var isRegistered: Bool = false
    @Published var transportType: TransportType = .Udp

    // MARK: - Call State

    @Published private(set) var callStatusMessage: String = ""
    @Published private(set) var isIncomingCall: Bool = false
    @Published private(set) var isCallActive: Bool = false
    @Published private(set) var isVideoEnabled: Bool = false
    @Published private(set) var remoteAddress: String = ""
    @Published private(set) var isSpeakerEnabled: Bool = false
    @Published private(set) var isMicrophoneEnabled: Bool = true
    @Published private(set) var activeCall: Call?

    // MARK: - CallKit

    let defaultCallerName = "Appel entrant"
    private var pendingCall: Call?
    private(set) var callKitDelegate: CallKitProviderDelegate!

    // MARK: - Video Views

    @Published var localVideoView: UIView?
    @Published var remoteVideoView: UIView?
    private var isVideoSetupComplete = false

    // MARK: - Initialization

    private init() {
        print("[LinphoneManager] Initializing...")

        LoggingService.Instance.logLevel = .Debug

        let factory = Factory.Instance
        let configDir = factory.getConfigDir(context: nil)

        do {
            core = try factory.createCore(
                configPath: "\(configDir)/ICondoConfig",
                factoryConfigPath: "",
                systemContext: nil
            )
        } catch {
            fatalError("[LinphoneManager] Failed to create Core: \(error)")
        }

        callKitDelegate = CallKitProviderDelegate(manager: self)

        configureCore()
        configureVideoCodecs()
        startCore()
        setupCoreDelegate()

        print("[LinphoneManager] Initialized successfully")
    }

    // MARK: - Core Configuration

    private func configureCore() {
        // CallKit & Push
        core.callkitEnabled = true
        core.pushNotificationEnabled = true

        // Video
        core.videoCaptureEnabled = true
        core.videoDisplayEnabled = true

        // Video Activation Policy
        do {
            let videoPolicy = try Factory.Instance.createVideoActivationPolicy()
            videoPolicy.automaticallyAccept = true
            videoPolicy.automaticallyInitiate = true
            core.videoActivationPolicy = videoPolicy
        } catch {
            print("[LinphoneManager] Failed to create video policy: \(error)")
        }

        // Audio
        core.echoCancellationEnabled = true
        core.adaptiveRateControlEnabled = true
        core.micEnabled = true
    }

    private func configureVideoCodecs() {
        let codecs = core.videoPayloadTypes
        print("[LinphoneManager] Configuring video codecs:")

        for codec in codecs {
            // Enable H264 and VP8
            if codec.mimeType == "H264" || codec.mimeType == "VP8" {
                codec.enable(enabled: true)
                print("[LinphoneManager]   - \(codec.mimeType): enabled")
            }
        }
    }

    private func startCore() {
        do {
            try core.start()
            print("[LinphoneManager] Core started successfully")
        } catch {
            print("[LinphoneManager] Failed to start Core: \(error)")
        }
    }

    // MARK: - Core Delegate

    private func setupCoreDelegate() {
        coreDelegate = CoreDelegateStub(
            onCallStateChanged: { [weak self] (core, call, state, message) in
                guard let self = self else { return }

                print("[LinphoneManager] Call state: \(state) - \(message)")

                DispatchQueue.main.async {
                    self.callStatusMessage = message
                    self.handleCallStateChange(call: call, state: state)
                }
            },
            onAccountRegistrationStateChanged: { [weak self] (core, account, state, message) in
                guard let self = self else { return }

                print("[LinphoneManager] Registration state: \(state) - \(message)")

                DispatchQueue.main.async {
                    switch state {
                    case .Ok:
                        self.isRegistered = true
                        if let token = account.params?.pushNotificationConfig?.voipToken {
                            print("[LinphoneManager] VoIP token: \(token)")
                        }
                    case .Cleared:
                        self.isRegistered = false
                    default:
                        break
                    }
                }
            }
        )

        core.addDelegate(delegate: coreDelegate)
    }

    // MARK: - Call State Handling

    private func handleCallStateChange(call: Call, state: Call.State) {
        switch state {
        case .PushIncomingReceived:
            print("[LinphoneManager] Push incoming received")
            pendingCall = call
            isIncomingCall = true
            callKitDelegate.reportIncomingCall()

        case .IncomingReceived:
            print("[LinphoneManager] Incoming call received")
            pendingCall = call
            isIncomingCall = true

            // Configure video params for incoming call
            do {
                let params = try core.createCallParams(call: call)
                params.videoEnabled = true
                params.videoDirection = MediaDirection.SendRecv
            } catch {
                print("[LinphoneManager] Failed to configure incoming call params: \(error)")
            }

            callKitDelegate.reportIncomingCall()
            remoteAddress = call.remoteAddress?.asStringUriOnly() ?? "Unknown"
            activeCall = call

        case .Connected:
            print("[LinphoneManager] Call connected")
            isIncomingCall = false
            isCallActive = true
            activeCall = call
            remoteAddress = call.remoteAddress?.asStringUriOnly() ?? "Unknown"

        case .StreamsRunning:
            print("[LinphoneManager] Streams running")
            isIncomingCall = false
            isCallActive = true
            activeCall = call
            setupVideoViews()

        case .Released, .End, .Error:
            print("[LinphoneManager] Call ended: \(state)")
            if isCallActive {
                callKitDelegate.endCall()
            }
            resetCallState()

        default:
            break
        }
    }

    // MARK: - Video Setup

    func setupVideoViews() {
        guard let call = core.currentCall else {
            print("[LinphoneManager] No current call for video setup")
            return
        }

        print("[LinphoneManager] Setting up video views...")

        let localView = UIView()
        let remoteView = UIView()

        localView.backgroundColor = .clear
        remoteView.backgroundColor = .black

        core.nativeVideoWindowId = UnsafeMutableRawPointer(Unmanaged.passUnretained(remoteView).toOpaque())
        core.nativePreviewWindowId = UnsafeMutableRawPointer(Unmanaged.passUnretained(localView).toOpaque())

        DispatchQueue.main.async {
            self.localVideoView = localView
            self.remoteVideoView = remoteView
            self.isVideoEnabled = true
            self.isVideoSetupComplete = true
        }

        print("[LinphoneManager] Video views configured")
    }

    private func resetCallState() {
        isCallActive = false
        isIncomingCall = false
        isVideoEnabled = false
        remoteAddress = ""
        activeCall = nil
        pendingCall = nil

        // Clean up video
        if isVideoSetupComplete {
            core.nativeVideoWindowId = nil
            core.nativePreviewWindowId = nil
            localVideoView = nil
            remoteVideoView = nil
            isVideoSetupComplete = false
        }

        print("[LinphoneManager] Call state reset")
    }

    // MARK: - Authentication

    func login(username: String? = nil, password: String? = nil, domain: String? = nil) {
        let user = username ?? self.username
        let pass = password ?? self.password
        let dom = domain ?? self.domain

        print("[LinphoneManager] Logging in: \(user)@\(dom)")

        do {
            let authInfo = try Factory.Instance.createAuthInfo(
                username: user,
                userid: user,
                passwd: pass,
                ha1: nil,
                realm: nil,
                domain: dom
            )

            let accountParams = try core.createAccountParams()

            let identity = try Factory.Instance.createAddress(addr: "sip:\(user)@\(dom)")
            try identity.setTransport(newValue: transportType)
            try accountParams.setIdentityaddress(newValue: identity)

            let server = try Factory.Instance.createAddress(addr: "sip:\(dom)")
            try server.setTransport(newValue: transportType)
            try accountParams.setServeraddress(newValue: server)

            accountParams.registerEnabled = true
            accountParams.pushNotificationAllowed = true
            accountParams.pushNotificationConfig?.provider = "apns.dev"

            core.addAuthInfo(info: authInfo)
            account = try core.createAccount(params: accountParams)
            try core.addAccount(account: account!)
            core.defaultAccount = account

            print("[LinphoneManager] Login configured successfully")

        } catch {
            print("[LinphoneManager] Login error: \(error)")
        }
    }

    // MARK: - Call Actions

    func makeCall(to address: String, withVideo: Bool = false) {
        print("[LinphoneManager] Making call to: \(address) (video: \(withVideo))")

        do {
            let sipAddress = try Factory.Instance.createAddress(addr: address)
            let params = try core.createCallParams(call: nil)
            params.videoEnabled = withVideo
            _ = core.inviteAddressWithParams(addr: sipAddress, params: params)
        } catch {
            print("[LinphoneManager] Failed to make call: \(error)")
        }
    }

    func acceptCall() {
        print("[LinphoneManager] Accepting call...")

        do {
            guard let call = pendingCall ?? core.currentCall else {
                print("[LinphoneManager] No call to accept")
                return
            }

            let params = try core.createCallParams(call: call)
            params.videoEnabled = true
            params.videoDirection = MediaDirection.SendRecv
            try call.acceptWithParams(params: params)

            print("[LinphoneManager] Call accepted with video")
        } catch {
            print("[LinphoneManager] Failed to accept call: \(error)")
        }
    }

    func endCall() {
        print("[LinphoneManager] Ending call...")

        do {
            if let call = core.currentCall ?? pendingCall {
                try call.terminate()
            }
        } catch {
            print("[LinphoneManager] Failed to end call: \(error)")
        }
    }

    // MARK: - Call Controls

    func toggleVideo() {
        guard let call = core.currentCall else { return }

        do {
            let params = try core.createCallParams(call: call)
            params.videoEnabled = !isVideoEnabled
            try call.update(params: params)
            print("[LinphoneManager] Video toggled")
        } catch {
            print("[LinphoneManager] Failed to toggle video: \(error)")
        }
    }

    func switchCamera() {
        let devices = core.videoDevicesList

        // Filter valid camera devices
        let validDevices = devices.filter { device in
            !device.contains("StaticImage") &&
            !device.contains("Static picture") &&
            !device.isEmpty
        }

        guard validDevices.count >= 2,
              let currentDevice = core.videoDevice,
              let currentIndex = validDevices.firstIndex(of: currentDevice) else {
            print("[LinphoneManager] Cannot switch camera")
            return
        }

        let nextIndex = (currentIndex + 1) % validDevices.count
        let nextDevice = validDevices[nextIndex]

        do {
            try core.setVideodevice(newValue: nextDevice)
            print("[LinphoneManager] Camera switched to: \(nextDevice)")
        } catch {
            print("[LinphoneManager] Failed to switch camera: \(error)")
        }
    }

    func toggleSpeaker() {
        do {
            let session = AVAudioSession.sharedInstance()
            if isSpeakerEnabled {
                try session.overrideOutputAudioPort(.none)
            } else {
                try session.overrideOutputAudioPort(.speaker)
            }
            isSpeakerEnabled.toggle()
            print("[LinphoneManager] Speaker: \(isSpeakerEnabled)")
        } catch {
            print("[LinphoneManager] Failed to toggle speaker: \(error)")
        }
    }

    func toggleMicrophone() {
        core.micEnabled.toggle()
        isMicrophoneEnabled = core.micEnabled
        print("[LinphoneManager] Microphone: \(isMicrophoneEnabled)")
    }

    // MARK: - Logout

    func logout(completion: (() -> Void)? = nil) {
        print("[LinphoneManager] Starting logout...")

        // 1. End any active call
        if isCallActive || isIncomingCall {
            endCall()
        }

        // 2. Disable push and unregister
        if let account = core.defaultAccount {
            do {
                let params = account.params?.clone()
                params?.pushNotificationAllowed = false
                params?.remotePushNotificationAllowed = false
                params?.registerEnabled = false
                account.params = params
            }
        }

        // 3. Clean up after delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
            guard let self = self else { return }

            self.core.removeDelegate(delegate: self.coreDelegate)
            self.core.clearAccounts()
            self.core.clearAllAuthInfo()

            self.resetCallState()
            self.isRegistered = false

            print("[LinphoneManager] Logout complete")
            completion?()
        }
    }

    func unregister() {
        guard let account = core.defaultAccount else { return }
        let params = account.params?.clone()
        params?.registerEnabled = false
        account.params = params
    }
}
