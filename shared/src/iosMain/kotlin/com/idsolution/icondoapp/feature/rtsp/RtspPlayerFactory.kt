package com.idsolution.icondoapp.feature.rtsp

import platform.UIKit.UIViewController

interface RtspPlayerFactory {
    fun createRtspPlayer(
        rtspUrl: String,
        onPlayerReady: (() -> Unit)?
    ): UIViewController
}