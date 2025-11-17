package com.idsolution.icondoapp.feature.rtsp

import androidx.compose.runtime.staticCompositionLocalOf

val LocalRtspPlayerFactory = staticCompositionLocalOf<RtspPlayerFactory> {
    error("No RtspPlayerFactory provided")
}