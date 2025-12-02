package com.idsolution.icondoapp.feature.rtsp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun RtspPlayer(
    rtspUrl: String,
    modifier: Modifier = Modifier
)