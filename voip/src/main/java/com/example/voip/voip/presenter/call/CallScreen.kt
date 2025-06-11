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
import org.linphone.mediastream.video.capture.CaptureTextureView

@Composable
fun CallScreen(
    phoneNumber: String,
    call: Call.State,
    //onIncomingCall: ((String) -> Unit),
    onEndCall: () -> Unit,
    onInitVideo: ((TextureView, CaptureTextureView) -> Unit),
    onToggleVideo: () -> Unit,
    onToggleCamera: () -> Unit
) {
    var isVideoEnabled by remember { mutableStateOf(false) }
    println("call: $call")
    when (call) {
        Call.State.OutgoingRinging, Call.State.Idle, Call.State.OutgoingInit, Call.State.OutgoingProgress -> {
            // Écran d'appel sortant
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

                    // Affichage du numéro de téléphone
                    Text(
                        text = "Calling $phoneNumber",
                        color = Color.White,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Boutons de contrôle
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Bouton vidéo
                        IconButton(
                            onClick = {
                                isVideoEnabled = !isVideoEnabled
                                onToggleVideo()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Video",
                                tint = Color.White
                            )
                        }

                        // Bouton fin d'appel
                        IconButton(
                            onClick = onEndCall,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        Call.State.PushIncomingReceived, Call.State.IncomingReceived, Call.State.IncomingEarlyMedia -> {
            // Appel en réception - déléguer à un autre composant via callback
           // onIncomingCall.invoke(call.name)
        }

        Call.State.Connected, Call.State.StreamsRunning -> {
            // Appel connecté - afficher les vues vidéo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                // Vues vidéo (si la vidéo est activée)
                TextureViewScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    onTextureAvailable = {
                        isVideoEnabled = true
                        onToggleVideo()
                    },
                    onInitVideo = onInitVideo
                )

                // Superposer les contrôles
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Boutons de contrôle pour l'appel en cours
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Bouton vidéo
                        IconButton(
                            onClick = {
                                isVideoEnabled = !isVideoEnabled
                                onToggleVideo()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Video",
                                tint = Color.White
                            )
                        }

                        // Bouton fin d'appel
                        IconButton(
                            onClick = onEndCall,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White
                            )
                        }

                        // Bouton bascule caméra (avant/arrière)
                        IconButton(
                            onClick = onToggleCamera,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Gray, CircleShape),
                            enabled = isVideoEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Call.State.Released -> {
            // Appel terminé
            onEndCall.invoke()
        }

        else -> {
            // États non gérés spécifiquement
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Call state: ${call.name}",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CallScreenPreview() {
    CallScreen(
        phoneNumber = "123 456 789",
        call = Call.State.Idle,
        //onIncomingCall = {},
        onEndCall = {},
        onInitVideo = { _, _ -> },
        onToggleVideo = {},
        onToggleCamera = {}
    )
}