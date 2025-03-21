package com.example.testkmpapp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import com.example.testkmpapp.feature.mainscreen.NavigationRoot
val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("LocalNativeViewFactory not initialized")
}
fun MainViewController(
    nativeViewFactory: NativeViewFactory
) = ComposeUIViewController(configure = {
    enforceStrictPlistSanityCheck = false
}) {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        NavigationRoot(onIncomingCall = {})
    }
}