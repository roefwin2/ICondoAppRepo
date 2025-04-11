package com.example.testkmpapp

import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import platform.UIKit.UIViewController

interface NativeViewFactory {

    fun createVoipView(
        label: String,
        phoneBook: Array<PhoneBook>,
        onClickListener: () -> Unit
    ): UIViewController
}