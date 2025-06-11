package com.idsolution.icondoapp.feature.voip

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.example.testkmpapp.LocalNativeViewFactory
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

@Composable
actual fun NativeVoipScreen(phoneBook: List<PhoneBook>) {
    val factory = LocalNativeViewFactory.current
        UIKitViewController(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            factory = {
                println("listPhoneBook: $phoneBook")
                factory.createVoipView(label = "Voip", phoneBook = phoneBook.toTypedArray(), onClickListener = {})
            },
            update = {
                println("listPhoneBook update: $phoneBook")
                factory.createVoipView(label = "Voip", phoneBook = phoneBook.toTypedArray(), onClickListener = {})
            },
            properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true)
        )
}
