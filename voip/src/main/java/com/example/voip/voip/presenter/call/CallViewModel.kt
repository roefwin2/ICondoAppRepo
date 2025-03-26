package com.example.voip.voip.presenter.call

import android.view.TextureView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.domain.models.ICondoCall
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.linphone.core.Call
import org.linphone.mediastream.video.capture.CaptureTextureView

class CallViewModel(
    private val voip: ICondoVoip
) : ViewModel() {

    val callState = voip.callState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ICondoCall()
    )

    fun initVideo(textureView: TextureView, captureTextureView: CaptureTextureView) {
        voip.initVideo(textureView, captureTextureView)
    }

    fun toggleVideo() {
        voip.toggleCamera()
    }

    fun hangUp() {
        voip.hangUp()
    }
}