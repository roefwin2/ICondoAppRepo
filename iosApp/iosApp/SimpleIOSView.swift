//
//  SimpleIOSView.swift
//  iosApp
//
//  Factory pour creer les vues natives iOS depuis Kotlin
//

import SwiftUI
import shared

/// Factory pour creer les vues natives iOS utilisees par Compose Multiplatform
final class IOSNativeViewFactory: NativeViewFactory {

    // MARK: - Singleton

    static let shared = IOSNativeViewFactory()

    // MARK: - Initialization

    private init() {
        print("[IOSNativeViewFactory] Initialized")
    }

    // MARK: - NativeViewFactory

    func createVoipView(
        label: String,
        phoneBook: KotlinArray<PhoneBook>,
        onClickListener: @escaping () -> Void
    ) -> UIViewController {
        print("[IOSNativeViewFactory] Creating VoIP view...")

        let contactsScreen = ContactsScreen(phoneBook: phoneBook)
            .environmentObject(LinphoneManager.shared)

        let hostingController = UIHostingController(rootView: contactsScreen)

        // Create container controller
        let containerController = UIViewController()
        containerController.addChild(hostingController)
        containerController.view.addSubview(hostingController.view)
        hostingController.didMove(toParent: containerController)

        // Setup constraints
        hostingController.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            hostingController.view.topAnchor.constraint(equalTo: containerController.view.safeAreaLayoutGuide.topAnchor),
            hostingController.view.bottomAnchor.constraint(equalTo: containerController.view.safeAreaLayoutGuide.bottomAnchor),
            hostingController.view.leadingAnchor.constraint(equalTo: containerController.view.safeAreaLayoutGuide.leadingAnchor),
            hostingController.view.trailingAnchor.constraint(equalTo: containerController.view.safeAreaLayoutGuide.trailingAnchor)
        ])

        print("[IOSNativeViewFactory] VoIP view created")
        return containerController
    }
}
