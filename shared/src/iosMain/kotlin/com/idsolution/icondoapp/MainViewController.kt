package com.example.testkmpapp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import com.bouyahya.kmpcall.di.initKoin
import com.example.testkmpapp.feature.mainscreen.NavigationRoot
import com.idsolution.icondoapp.NativeVoipLogin
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIViewController
import platform.posix.err

// Extension pour iOS qui ajoute la fonctionnalité d'affichage d'alertes
class iOSNativeViewFactory(private val viewController: UIViewController) {
    // Fonction pour afficher une alerte iOS native
    fun showAlert(title: String, message: String) {
        val alertController = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )

        alertController.addAction(
            UIAlertAction.actionWithTitle(
                title = "OK",
                style = UIAlertActionStyleDefault,
                handler = null
            )
        )

        viewController.presentViewController(alertController, animated = true, completion = null)
    }
}

val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("LocalNativeViewFactory not initialized")
}
val LocalVoipLoginFactory = staticCompositionLocalOf<NativeVoipLogin> {
    error("LocalVoipLoginFactory not initialized")
}

fun MainViewController(
    viewController: UIViewController,
    nativeViewFactory: NativeViewFactory,
) : UIViewController {
    return ComposeUIViewController(configure = {
        enforceStrictPlistSanityCheck = false
    }) {

        CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
            NavigationRoot(onIncomingCall = {}, onErrorLogin = { errormsg ->
                iOSNativeViewFactory(viewController).showAlert("Erreur de connexion", errormsg)
            })
        }
    }
}