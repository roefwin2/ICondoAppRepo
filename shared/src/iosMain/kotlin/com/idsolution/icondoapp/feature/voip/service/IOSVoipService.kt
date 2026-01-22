package com.idsolution.icondoapp.feature.voip.service

import com.idsolution.icondoapp.feature.voip.domain.CallState
import com.idsolution.icondoapp.feature.voip.domain.NativeVoipHandler
import com.idsolution.icondoapp.feature.voip.domain.RegistrationState
import com.idsolution.icondoapp.feature.voip.domain.VoipAccountState
import com.idsolution.icondoapp.feature.voip.domain.VoipCallInfo
import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.domain.VoipStateCallback
import com.idsolution.icondoapp.feature.voip.domain.VoipTransportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * iOS implementation of VoipService using Linphone SDK via CocoaPods
 *
 * Uses NativeVoipHandler from commonMain for native Swift interop.
 * The Swift LinphoneManager implements NativeVoipHandler interface.
 */
class IOSVoipService(
    private val eventHandler: VoipEventHandler? = null
) : VoipService, VoipStateCallback {

    private val TAG = "[ICondo-VoIP-iOS]"

    private var isInitialized = false

    private val _callState = MutableStateFlow(VoipCallInfo())
    override val callState: StateFlow<VoipCallInfo> = _callState.asStateFlow()

    private val _accountState = MutableStateFlow(VoipAccountState())
    override val accountState: StateFlow<VoipAccountState> = _accountState.asStateFlow()

    // Reference to native iOS VoIP handler (set from Swift LinphoneManager)
    private var nativeHandler: NativeVoipHandler? = null

    override fun initialize() {
        if (isInitialized) {
            println("$TAG VoIP service already initialized")
            return
        }

        println("$TAG Initializing iOS VoIP service")
        nativeHandler?.initialize()
        isInitialized = true
        println("$TAG iOS VoIP service initialized")
    }

    override fun login(
        username: String,
        password: String,
        domain: String,
        transportType: VoipTransportType
    ) {
        println("$TAG Login: $username@$domain")

        val transport = when (transportType) {
            VoipTransportType.UDP -> "udp"
            VoipTransportType.TCP -> "tcp"
            VoipTransportType.TLS -> "tls"
        }

        nativeHandler?.login(username, password, domain, transport)
    }

    override fun logout() {
        println("$TAG Logging out")
        nativeHandler?.logout()
        _accountState.value = VoipAccountState()
        _callState.value = VoipCallInfo()
    }

    override fun makeCall(sipUri: String) {
        println("$TAG Making call to: $sipUri")
        nativeHandler?.makeCall(sipUri)
    }

    override fun answerCall() {
        println("$TAG Answering call")
        nativeHandler?.answerCall()
    }

    override fun hangUp() {
        println("$TAG Hanging up")
        nativeHandler?.hangUp()
    }

    override fun toggleVideo() {
        println("$TAG Toggle video")
        nativeHandler?.toggleVideo()
        _callState.update { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    override fun toggleCamera() {
        println("$TAG Toggle camera")
        nativeHandler?.toggleCamera()
        _callState.update { it.copy(isCameraFront = !it.isCameraFront) }
    }

    override fun pauseOrResume() {
        println("$TAG Pause/Resume")
        nativeHandler?.pauseOrResume()
    }

    override fun toggleMute() {
        println("$TAG Toggle mute")
        nativeHandler?.toggleMute()
        _callState.update { it.copy(isAudioMuted = !it.isAudioMuted) }
    }

    override fun toggleSpeaker() {
        println("$TAG Toggle speaker")
        nativeHandler?.toggleSpeaker()
    }

    override fun isInitialized(): Boolean = isInitialized

    override fun isRegistered(): Boolean = _accountState.value.isRegistered

    override fun startBackgroundService() {
        // iOS handles background VoIP via PushKit - no explicit service needed
        println("$TAG Background service managed by iOS PushKit")
    }

    override fun stopBackgroundService() {
        // iOS handles this automatically
        println("$TAG Background service handled by iOS")
    }

    override fun destroy() {
        println("$TAG Destroying iOS VoIP service")
        nativeHandler?.destroy()
        isInitialized = false
    }

    // ========== Methods called from Swift ==========

    /**
     * Set the native iOS handler (called from Swift LinphoneManager at app start)
     */
    fun setNativeHandler(handler: NativeVoipHandler) {
        this.nativeHandler = handler
        println("$TAG Native handler set")
    }

    // ========== VoipStateCallback implementation ==========

    override fun onRegistrationStateChanged(state: String, message: String, isRegistered: Boolean) {
        val mappedState = when (state) {
            "none" -> RegistrationState.NONE
            "progress" -> RegistrationState.PROGRESS
            "ok" -> RegistrationState.OK
            "cleared" -> RegistrationState.CLEARED
            "failed" -> RegistrationState.FAILED
            else -> RegistrationState.NONE
        }

        _accountState.update {
            VoipAccountState(
                registrationState = mappedState,
                message = message,
                isRegistered = isRegistered
            )
        }
    }

    override fun onCallStateChanged(
        state: String,
        remoteAddress: String?,
        remoteName: String?,
        isVideoEnabled: Boolean,
        duration: Long
    ) {
        val mappedState = mapCallState(state)

        _callState.update {
            VoipCallInfo(
                state = mappedState,
                remoteAddress = remoteAddress,
                remoteName = remoteName,
                isVideoEnabled = isVideoEnabled,
                duration = duration
            )
        }

        // Notify event handler
        when (mappedState) {
            CallState.INCOMING_RECEIVED -> {
                eventHandler?.onIncomingCall(remoteAddress ?: "", remoteName)
            }
            CallState.STREAMS_RUNNING, CallState.CONNECTED -> {
                eventHandler?.onCallConnected()
            }
            CallState.END, CallState.RELEASED -> {
                eventHandler?.onCallEnded()
            }
            else -> {}
        }
    }

    // Legacy methods for backwards compatibility
    fun updateRegistrationState(state: String, message: String, isRegistered: Boolean) {
        onRegistrationStateChanged(state, message, isRegistered)
    }

    fun updateCallState(
        state: String,
        remoteAddress: String?,
        remoteName: String?,
        isVideoEnabled: Boolean,
        duration: Long
    ) {
        onCallStateChanged(state, remoteAddress, remoteName, isVideoEnabled, duration)
    }

    private fun mapCallState(state: String): CallState {
        return when (state.lowercase()) {
            "idle" -> CallState.IDLE
            "outgoing_init" -> CallState.OUTGOING_INIT
            "outgoing_progress" -> CallState.OUTGOING_PROGRESS
            "outgoing_ringing" -> CallState.OUTGOING_RINGING
            "outgoing_early_media" -> CallState.OUTGOING_EARLY_MEDIA
            "incoming_received" -> CallState.INCOMING_RECEIVED
            "incoming_early_media" -> CallState.INCOMING_EARLY_MEDIA
            "connected" -> CallState.CONNECTED
            "streams_running" -> CallState.STREAMS_RUNNING
            "pausing" -> CallState.PAUSING
            "paused" -> CallState.PAUSED
            "resuming" -> CallState.RESUMING
            "updating" -> CallState.UPDATING
            "updated_by_remote" -> CallState.UPDATED_BY_REMOTE
            "error" -> CallState.ERROR
            "end" -> CallState.END
            "released" -> CallState.RELEASED
            else -> CallState.IDLE
        }
    }
}
