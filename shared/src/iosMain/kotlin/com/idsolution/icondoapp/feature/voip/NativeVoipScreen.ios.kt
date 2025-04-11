package com.idsolution.icondoapp.feature.voip

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import com.example.testkmpapp.LocalNativeViewFactory
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

@Composable
actual fun NativeVoipScreen(phoneBook: List<PhoneBook>) {
    val factory = LocalNativeViewFactory.current
    UIKitViewController(
        modifier = Modifier.fillMaxSize(),
        factory = {
            println("listPhoneBook: $phoneBook")
            factory.createVoipView(label = "Voip",phoneBook = phoneBook.toTypedArray(), onClickListener = {})
        },
        update = {
            println("listPhoneBook update: $phoneBook")
            factory.createVoipView(label = "Voip",phoneBook = phoneBook.toTypedArray(), onClickListener = {})
        }
    )
}