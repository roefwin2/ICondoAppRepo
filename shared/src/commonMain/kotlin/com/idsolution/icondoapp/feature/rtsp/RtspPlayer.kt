package com.idsolution.icondoapp.feature.rtsp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun RtspPlayer(
    rtspUrl: String,
    modifier: Modifier = Modifier
)

// Interface commune pour contrôler le lecteur
expect class RtspPlayerController {
    fun play()
    fun pause()
    fun stop()
    fun release()
}