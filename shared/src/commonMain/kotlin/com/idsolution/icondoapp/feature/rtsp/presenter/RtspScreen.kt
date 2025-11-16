package com.idsolution.icondoapp.feature.rtsp.presenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.idsolution.icondoapp.feature.rtsp.RtspPlayer

@Composable
fun CameraScreen() {
    var isPlaying by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        RtspPlayer(
            rtspUrl = "rtsp://admin:12345678@70.80.241.53:554/ch01/0",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { isPlaying = !isPlaying }) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}