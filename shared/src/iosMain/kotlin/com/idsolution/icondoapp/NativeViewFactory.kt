package com.example.testkmpapp

import platform.UIKit.UIViewController

interface NativeViewFactory {

    fun createVoipView(
        label : String,
        onClickListener : () -> Unit
    ) : UIViewController
}