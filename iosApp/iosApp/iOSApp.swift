import SwiftUI
import shared

@main
struct iosAppApp: App {
    @StateObject private var tutorialContext = OutgoingCallTutorialContext()
    
    init() {
        do {
            try InitKoinKt.doInitKoin()
        }catch{
            print("Erreur lors de l'initialisation de Koin: \(error)")
        }
        }
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .environmentObject(tutorialContext)
        }
    }
}
