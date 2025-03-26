package com.example.voip.voip.presenter.call

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voip.voip.presenter.TextureViewScreen
import org.linphone.core.Call
import org.linphone.core.Call.State
import org.linphone.core.tools.service.CoreService
import org.linphone.mediastream.video.capture.CaptureTextureView

@Composable
fun CallScreen(
    phoneNumber: String,
    call: Call.State,
    onIncomingCall: ((String) -> Unit),
    onEndCall: () -> Unit,
    onInitVideo: ((TextureView, CaptureTextureView) -> Unit),
    onToggleCamera: (Boolean) -> Unit
) {
    var isCameraEnabled by remember { mutableStateOf(false) }
    when (call) {
        State.OutgoingRinging, State.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Phone number display
                    Text(
                        text = "Calling $phoneNumber",
                        color = Color.White,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                isCameraEnabled = !isCameraEnabled
                                onToggleCamera(isCameraEnabled)
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isCameraEnabled) {
                                    Icons.Default.Videocam // Replace with appropriate icon
                                } else {
                                    Icons.Default.VideocamOff // Replace with appropriate icon
                                },
                                contentDescription = "Toggle Camera",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = onEndCall,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd, // Replace with appropriate icon
                                contentDescription = "End Call",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        State.PushIncomingReceived,State.IncomingReceived,State.IncomingEarlyMedia -> {
            onIncomingCall.invoke(call.name)
        }


        State.Connected -> {
            TextureViewScreen(modifier = Modifier.background(Color.Gray),
                onTextureAvailable = {

                }) { textureView, captureTextureView ->
                onInitVideo.invoke(textureView, captureTextureView)
            }
        }

        State.Released -> onEndCall.invoke()
        else -> {
            Text(text = call.name)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CallScreenPreview() {
    CallScreen(
        phoneNumber = "123 456 789",
        call = State.Idle,
        onIncomingCall = {},
        onEndCall = { /* Preview action */ },
        onInitVideo = { _, _ ->
        },
        onToggleCamera = { /* Preview toggle */ }
    )
}

