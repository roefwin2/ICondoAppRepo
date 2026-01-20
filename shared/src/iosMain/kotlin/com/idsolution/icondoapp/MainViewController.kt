package com.idsolution.icondoapp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import com.idsolution.icondoapp.feature.mainscreen.NavigationRoot
import com.idsolution.icondoapp.NativeVoipLogin
import com.idsolution.icondoapp.feature.rtsp.LocalRtspPlayerFactory
import com.idsolution.icondoapp.feature.rtsp.RtspPlayerFactory
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
    rtspPlayerFactory: RtspPlayerFactory
) = ComposeUIViewController(configure = {
    enforceStrictPlistSanityCheck = false
}) {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory, LocalRtspPlayerFactory provides rtspPlayerFactory) {
        NavigationRoot(onIncomingCall = {}, onErrorLogin = { errormsg ->
            iOSNativeViewFactory(viewController).showAlert("Erreur de connexion", errormsg)
        })
    }
}