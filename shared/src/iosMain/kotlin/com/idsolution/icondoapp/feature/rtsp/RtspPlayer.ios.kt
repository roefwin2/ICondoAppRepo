package com.idsolution.icondoapp.feature.rtsp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController

@Composable
actual fun RtspPlayer(
    rtspUrl: String,
    modifier: Modifier,
) {
    val factory = LocalRtspPlayerFactory.current
    var playerState by remember { mutableStateOf("INIT") }

    Box(modifier = modifier.background(Color.Black)) {
        UIKitViewController(
            modifier = Modifier.fillMaxSize(),
            factory = {
                println("🎥 Creating RTSP Player for: $rtspUrl")
                playerState = "LOADING"

                factory.createRtspPlayer(
                    rtspUrl = rtspUrl,
                    onPlayerReady = {
                        println("✅ RTSP Player ready")
                        playerState = "PLAYING"
                        //onControllerReady?.invoke(IosRtspPlayerController())
                    }
                )
            },
            update = { viewController ->
                println("🔄 Updating RTSP Player")
            },
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true
            )
        )

        // Indicateur d'état
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            color = Color.Black.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🍎", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = playerState,
                    color = when (playerState) {
                        "PLAYING" -> Color.Green
                        "LOADING" -> Color.Yellow
                        "ERROR" -> Color.Red
                        else -> Color.White
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}