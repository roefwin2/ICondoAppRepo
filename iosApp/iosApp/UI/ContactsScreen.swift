//
//  ContactsScreen.swift
//  iosApp
//
//  Ecran d'affichage des contacts avec possibilite d'appel
//

import SwiftUI
import shared

/// Ecran affichant la liste des contacts avec options d'appel audio/video
struct ContactsScreen: View {

    // MARK: - Properties

    @EnvironmentObject var linphoneManager: LinphoneManager
    @StateObject private var viewModel = ContactsViewModel.shared
    @State private var isCallScreenPresented = false

    let phoneBook: KotlinArray<PhoneBook>

    // MARK: - Initialization

    init(phoneBook: KotlinArray<PhoneBook>) {
        self.phoneBook = phoneBook
    }

    // MARK: - Body

    var body: some View {
        Group {
            if viewModel.contacts.isEmpty {
                EmptyContactsView()
            } else {
                ContactListView()
            }
        }
        .onAppear {
            viewModel.loadContacts(from: phoneBook)
        }
        .fullScreenCover(isPresented: $isCallScreenPresented) {
            CallScreen(onDismiss: {
                isCallScreenPresented = false
            })
            .environmentObject(linphoneManager)
        }
    }

    // MARK: - Subviews

    @ViewBuilder
    private func ContactListView() -> some View {
        List {
            Section(header: Text("Repertoire telephonique").font(.headline)) {
                ForEach(viewModel.contacts, id: \.dialno) { contact in
                    ContactRow(contact: contact)
                }
            }
        }
        .listStyle(PlainListStyle())
    }

    @ViewBuilder
    private func ContactRow(contact: PhoneBook) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(contact.firstname)
                    .font(.headline)

                Text(contact.dialno)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }

            Spacer()

            HStack(spacing: 15) {
                // Audio call button
                Button(action: {
                    makeCall(to: contact, withVideo: false)
                }) {
                    Image(systemName: "phone.fill")
                        .foregroundColor(.green)
                        .frame(width: 44, height: 44)
                }
                .buttonStyle(PlainButtonStyle())

                // Video call button
                Button(action: {
                    makeCall(to: contact, withVideo: true)
                }) {
                    Image(systemName: "video.fill")
                        .foregroundColor(.blue)
                        .frame(width: 44, height: 44)
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private func EmptyContactsView() -> some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "person.crop.circle.badge.exclamationmark")
                .font(.system(size: 60))
                .foregroundColor(.gray)

            Text("Aucun contact")
                .font(.headline)
                .foregroundColor(.gray)

            Text("Le repertoire est vide")
                .font(.subheadline)
                .foregroundColor(.gray.opacity(0.8))

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemGroupedBackground))
    }

    // MARK: - Actions

    private func makeCall(to contact: PhoneBook, withVideo: Bool) {
        print("[ContactsScreen] Making call to: \(contact.firstname) (\(contact.dialno)) - video: \(withVideo)")

        // Build SIP address
        let domain = linphoneManager.domain.isEmpty ? "montreal1.voip.ms" : linphoneManager.domain
        let sipAddress = "sip:\(contact.dialno)@\(domain)"

        // Make the call
        linphoneManager.makeCall(to: sipAddress, withVideo: withVideo)

        // Show call screen
        isCallScreenPresented = true
    }
}

// MARK: - ContactsViewModel

final class ContactsViewModel: ObservableObject {

    // MARK: - Singleton

    static let shared = ContactsViewModel()

    // MARK: - Published

    @Published var contacts: [PhoneBook] = []

    // MARK: - Initialization

    private init() {}

    // MARK: - Methods

    func loadContacts(from kotlinArray: KotlinArray<PhoneBook>) {
        var swiftArray: [PhoneBook] = []
        var seenDialNumbers = Set<String>()

        for i in 0..<kotlinArray.size {
            if let contact = kotlinArray.get(index: i) {
                // Avoid duplicates
                if !seenDialNumbers.contains(contact.dialno) {
                    swiftArray.append(contact)
                    seenDialNumbers.insert(contact.dialno)
                }
            }
        }

        print("[ContactsViewModel] Loaded \(swiftArray.count) contacts")
        self.contacts = swiftArray
    }
}
