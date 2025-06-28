//
//  SimpleIOSView.swift
//  iosApp
//
//  Created by Régis Dika on 21/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared

class IOSNativeViewFactory: NativeViewFactory {
    static var shared = IOSNativeViewFactory()
  
    @EnvironmentObject var tutorialContext: OutgoingCallTutorialContext
    
    func createVoipView(label: String, phoneBook: KotlinArray<PhoneBook>, onClickListener: @escaping () -> Void) -> UIViewController {
        print("Création du tutorialContext...")
        print("TutorialContext créé, initialisation de ContentView...")
        let vc = UIHostingController(rootView: ContentView(tutorialContext: OutgoingCallTutorialContext()))
        return vc
    }
}

struct SimpleIOSView: View {
    var label: String
    var action: () -> Void
    var body: some View {
        Button(action: action){
            Text(label)
        }
    }
}
