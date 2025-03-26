package com.example.voip.voip.domain

import android.view.TextureView
import com.example.voip.voip.data.AccountState
import com.example.voip.voip.domain.models.ICondoCall
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.Call
import org.linphone.core.TransportType
import org.linphone.mediastream.video.capture.CaptureTextureView

interface ICondoVoip {

    val callState : StateFlow<ICondoCall>
    val accountState : StateFlow<AccountState?>
    fun initVideo(textureView: TextureView,captureTextureView: CaptureTextureView)
    fun login(username: String,password: String,domain :String,transportType: TransportType)
    fun outgoingCall(remoteSipUri : String)

    fun answerCall()
    fun hangUp()
    fun startKeepAliveService()

    fun toggleVideo()
    fun toggleCamera()
    fun pauseOrResume()
}