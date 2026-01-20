//
//  iOSApp.swift
//  iosApp
//
//  Point d'entree de l'application iOS avec initialisation VoIP
//

import SwiftUI
import shared
import Combine

@main
struct ICondoApp: App {

    // MARK: - State

    @StateObject private var linphoneManager = LinphoneManager.shared
    @State private var isCallInterfacePresented = false
    @State private var cancellables = Set<AnyCancellable>()

    // MARK: - Listeners

    private let loginListener = VoipLoginListenerImpl()

    // MARK: - Initialization

    init() {
        print("[App] ========================================")
        print("[App] ICondoApp initializing...")
        print("[App] ========================================")

        do {
            // Initialize Koin
            try InitKoinKt.doInitKoin()
            print("[App] Koin initialized")

            // Configure VoIP login listener
            VoipLogin.shared.setListener(listener: loginListener)
            print("[App] VoIP login listener configured")

            // Load user info for push notifications
            VoIPPushManager.shared.loadUserInfo()
            print("[App] User info loaded")

            print("[App] ========================================")
            print("[App] Initialization complete!")
            print("[App] ========================================")

        } catch {
            print("[App] Initialization error: \(error)")
        }
    }

    // MARK: - Body

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .environmentObject(linphoneManager)
                .onAppear {
                    setupCallObservers()
                }
                .fullScreenCover(isPresented: $isCallInterfacePresented) {
                    CallScreen(onDismiss: {
                        isCallInterfacePresented = false
                    })
                    .environmentObject(linphoneManager)
                }
        }
    }

    // MARK: - Private Methods

    private func setupCallObservers() {
        print("[App] Setting up call observers...")

        // Observe call state changes
        linphoneManager.$isCallActive
            .receive(on: DispatchQueue.main)
            .sink { [self] isActive in
                print("[App] Call active: \(isActive)")
                handleCallStateChange(isActive: isActive)
            }
            .store(in: &cancellables)

        // Observe incoming calls
        linphoneManager.$isIncomingCall
            .receive(on: DispatchQueue.main)
            .sink { isIncoming in
                print("[App] Incoming call: \(isIncoming)")
            }
            .store(in: &cancellables)

        // Observe registration state
        linphoneManager.$isRegistered
            .receive(on: DispatchQueue.main)
            .sink { isRegistered in
                print("[App] VoIP registered: \(isRegistered)")
            }
            .store(in: &cancellables)

        print("[App] Call observers configured")
    }

    private func handleCallStateChange(isActive: Bool) {
        if isActive {
            print("[App] Call started - showing call interface")
            isCallInterfacePresented = true
        } else if !linphoneManager.isIncomingCall {
            print("[App] Call ended - hiding call interface")
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                self.isCallInterfacePresented = false
            }
        }
    }
}
