package com.idsolution.icondoapp.feature.voip

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import com.example.testkmpapp.LocalNativeViewFactory

@Composable
actual fun NativeVoipScreen() {
    val factory = LocalNativeViewFactory.current
    UIKitViewController(
        modifier = Modifier.fillMaxSize(),
        factory = {
            factory.createVoipView(label = "Voip", onClickListener = {})
        }
    )
}