package com.example.voip.voip.presenter.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel

@Composable
fun CallScreenRoot(
    callViewModel: CallViewModel = koinViewModel(),
    onIncomingCall: (String) -> Unit,
    onEndCall: () -> Unit
) {
    val state by callViewModel.callState.collectAsState()
    val isVideoEnabled by callViewModel.isVideoEnabled.collectAsState()

    // Extraire le numéro de téléphone depuis les informations d'appel si disponible
    val phoneNumber = state.call?.remoteAddress?.displayName
        ?: state.call?.remoteAddress?.username
        ?: ""

    CallScreen(
        phoneNumber = phoneNumber,
        call = state.state,
        //onIncomingCall = onIncomingCall,
        onEndCall = {
            callViewModel.hangUp()
            onEndCall()
        },
        onInitVideo = { textureView, captureTextureView ->
            callViewModel.initVideo(textureView, captureTextureView)
        },
        onToggleVideo = {
            callViewModel.toggleVideo()
        },
        onToggleCamera = {
            callViewModel.toggleCamera()
        }
    )
}