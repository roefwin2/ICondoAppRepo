package com.example.voip.voip.presenter.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun CallScreenRoot(
    callViewModel: CallViewModel = koinViewModel(),
    onIncomingCall: ((String) -> Unit),
    onEndCall: (() -> Unit)
) {
    val state = callViewModel.callState.collectAsState().value

    CallScreen(
        phoneNumber = "",
        call = state.state,
        onEndCall = {
            onEndCall.invoke()
            callViewModel.hangUp()
        },
        onInitVideo = { textureView, captureTextureView ->
            callViewModel.initVideo(textureView, captureTextureView)
        },
        onToggleCamera = {
            callViewModel.toggleVideo()
        },
        onToggleVideo = {
            callViewModel.toggleVideo()
        }
    )
}