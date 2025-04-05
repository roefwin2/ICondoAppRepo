package com.example.voip.voip.presenter.call

import android.util.Log
import android.view.TextureView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.domain.models.ICondoCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.TransportType
import org.linphone.mediastream.video.capture.CaptureTextureView

class CallViewModel(
    private val condoVoip: ICondoVoip
) : ViewModel() {
    private val TAG = "CallViewModel"

    private val _callState = MutableStateFlow(ICondoCall())
    val callState: StateFlow<ICondoCall> = _callState.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(false)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            condoVoip.callState.collectLatest { state ->
                _callState.value = state

                // Update video state when call state changes
                if (state.state == Call.State.StreamsRunning) {
                    _isVideoEnabled.value = state.call?.currentParams?.isVideoEnabled ?: false
                }
            }
        }
    }

    fun login(username: String, password: String, domain: String, transportType: TransportType = TransportType.Tls) {
        condoVoip.login(
            username = username,
            password = password,
            domain = domain,
            transportType = transportType
        )
    }

    fun outgoingCall(remoteSipUri: String) {
        condoVoip.outgoingCall(remoteSipUri)
    }

    fun answerCall() {
        condoVoip.answerCall()
    }

    fun hangUp() {
        condoVoip.hangUp()
    }

    fun initVideo(textureView: TextureView, captureTextureView: CaptureTextureView) {
        Log.d(TAG, "Initializing video with TextureView and CaptureTextureView")
        try {
            condoVoip.initVideo(textureView, captureTextureView)
            Log.d(TAG, "Video initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing video: ${e.message}", e)
        }
    }

    fun toggleVideo() {
        Log.d(TAG, "Toggling video")
        try {
            condoVoip.toggleVideo()
            // The state will be updated through the flow collection in init block
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling video: ${e.message}", e)
        }
    }

    fun toggleCamera() {
        Log.d(TAG, "Toggling camera")
        try {
            condoVoip.toggleCamera()
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling camera: ${e.message}", e)
        }
    }

    fun pauseOrResume() {
        condoVoip.pauseOrResume()
    }
}