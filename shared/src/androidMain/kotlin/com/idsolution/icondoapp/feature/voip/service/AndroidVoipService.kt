package com.idsolution.icondoapp.feature.voip.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.idsolution.icondoapp.feature.voip.domain.CallState
import com.idsolution.icondoapp.feature.voip.domain.RegistrationState
import com.idsolution.icondoapp.feature.voip.domain.VoipAccountState
import com.idsolution.icondoapp.feature.voip.domain.VoipCallInfo
import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.domain.VoipTransportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.ConsolidatedPresence
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.GlobalState
import org.linphone.core.LogCollectionState
import org.linphone.core.MediaDirection
import org.linphone.core.MediaEncryption
import org.linphone.core.Reason
import org.linphone.core.RegistrationState as LinphoneRegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log

/**
 * Android implementation of VoipService using Linphone SDK
 */
class AndroidVoipService(
    private val context: Context,
    private val eventHandler: VoipEventHandler? = null
) : VoipService {

    private val TAG = "[ICondo-VoIP]"

    private lateinit var core: Core
    private var isInitialized = false

    private val _callState = MutableStateFlow(VoipCallInfo())
    override val callState: StateFlow<VoipCallInfo> = _callState.asStateFlow()

    private val _accountState = MutableStateFlow(VoipAccountState())
    override val accountState: StateFlow<VoipAccountState> = _accountState.asStateFlow()

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: LinphoneRegistrationState?,
            message: String
        ) {
            Log.i(TAG, "Account registration state: ${state?.name} - $message")

            val mappedState = when (state) {
                LinphoneRegistrationState.None -> RegistrationState.NONE
                LinphoneRegistrationState.Progress -> RegistrationState.PROGRESS
                LinphoneRegistrationState.Ok -> RegistrationState.OK
                LinphoneRegistrationState.Cleared -> RegistrationState.CLEARED
                LinphoneRegistrationState.Failed -> RegistrationState.FAILED
                else -> RegistrationState.NONE
            }

            _accountState.update {
                VoipAccountState(
                    registrationState = mappedState,
                    message = message,
                    isRegistered = state == LinphoneRegistrationState.Ok
                )
            }

            if (core.globalState == GlobalState.Off) return

            if (state == LinphoneRegistrationState.Ok) {
                core.consolidatedPresence = ConsolidatedPresence.Online
                Log.i(TAG, "Account successfully registered")
                startBackgroundService()
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            Log.i(TAG, "Call state changed: ${state?.name} - $message")

            val mappedState = mapLinphoneCallState(state)

            _callState.update {
                VoipCallInfo(
                    state = mappedState,
                    remoteAddress = call.remoteAddress?.asStringUriOnly(),
                    remoteName = call.remoteAddress?.displayName,
                    isVideoEnabled = call.currentParams?.isVideoEnabled == true,
                    duration = call.duration.toLong()
                )
            }

            // Notify event handler
            when (state) {
                Call.State.IncomingReceived -> {
                    eventHandler?.onIncomingCall(
                        call.remoteAddress?.asStringUriOnly() ?: "",
                        call.remoteAddress?.displayName
                    )
                }
                Call.State.StreamsRunning, Call.State.Connected -> {
                    eventHandler?.onCallConnected()
                }
                Call.State.End, Call.State.Released -> {
                    eventHandler?.onCallEnded()
                }
                else -> {}
            }
        }
    }

    override fun initialize() {
        if (isInitialized) {
            Log.i(TAG, "VoIP service already initialized")
            return
        }

        Log.i(TAG, "Initializing Linphone Core")

        val factory = Factory.instance()
        factory.setDebugMode(true, "ICondo-VoIP")
        core = factory.createCore(null, null, context)

        // Network configuration
        core.isPushNotificationEnabled = true
        core.enableLogCollection(LogCollectionState.Enabled)
        core.isIpv6Enabled = false
        core.setUserAgent("ICondoApp", "2.0")

        // Video configuration
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true
        core.videoActivationPolicy.automaticallyAccept = true
        core.videoActivationPolicy.automaticallyInitiate = true

        // Timeouts
        core.incTimeout = 60000

        // Enable video codecs
        core.videoPayloadTypes.forEach { codec ->
            when (codec.mimeType) {
                "VP8", "H264" -> codec.enable(true)
                else -> codec.enable(false)
            }
        }

        core.isVideoAdaptiveJittcompEnabled = true
        core.addListener(coreListener)

        isInitialized = true
        Log.i(TAG, "Linphone Core initialized successfully")
    }

    override fun login(
        username: String,
        password: String,
        domain: String,
        transportType: VoipTransportType
    ) {
        if (!isInitialized) {
            Log.e(TAG, "VoIP service not initialized")
            return
        }

        Log.i(TAG, "Login: $username@$domain with transport: ${transportType.name}")

        val factory = Factory.instance()
        val identityUri = "sip:$username@$domain"

        // Clean up existing accounts
        core.accountList
            .filter { it.params.identityAddress?.asStringUriOnly() == identityUri }
            .forEach { core.removeAccount(it) }

        core.authInfoList
            .filter { it.username == username && (it.domain == domain || it.realm == domain) }
            .forEach { core.removeAuthInfo(it) }

        // Create auth info
        val auth = factory.createAuthInfo(username, null, password, null, null, domain, null)
        core.addAuthInfo(auth)

        // Account params
        val params = core.createAccountParams()
        params.identityAddress = factory.createAddress(identityUri)

        val linphoneTransport = when (transportType) {
            VoipTransportType.UDP -> TransportType.Udp
            VoipTransportType.TCP -> TransportType.Tcp
            VoipTransportType.TLS -> TransportType.Tls
        }

        val server = factory.createAddress("sip:$domain")!!.apply {
            transport = linphoneTransport
            port = when (linphoneTransport) {
                TransportType.Tls -> 5061
                else -> 5060
            }
        }

        params.serverAddress = server
        params.expires = 300
        params.isRegisterEnabled = true
        params.isOutboundProxyEnabled = true

        // NAT configuration for video
        params.natPolicy = core.createNatPolicy()
        params.natPolicy?.isStunEnabled = true
        params.natPolicy?.isIceEnabled = true

        val account = core.createAccount(params)

        core.clearAccounts()
        core.addAccount(account)
        core.ringDuringIncomingEarlyMedia = true
        core.defaultAccount = account

        if (core.globalState == GlobalState.Ready || core.globalState == GlobalState.Off) {
            try {
                core.start()
                Log.i(TAG, "Core started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting core: $e")
            }
        }
    }

    override fun logout() {
        Log.i(TAG, "Starting VoIP logout")

        try {
            // Terminate active calls
            core.calls.forEach { call ->
                try {
                    call.terminate()
                } catch (e: Exception) {
                    Log.e(TAG, "Error terminating call: $e")
                }
            }

            // Unregister from SIP server
            core.defaultAccount?.let { account ->
                try {
                    val params = account.params.clone()
                    params?.isRegisterEnabled = false
                    account.params = params
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering: $e")
                }
            }

            // Stop background service
            stopBackgroundService()

            // Clear accounts and auth
            core.clearAccounts()
            core.clearAllAuthInfo()

            // Reset states
            _accountState.value = VoipAccountState()
            _callState.value = VoipCallInfo()

            Log.i(TAG, "VoIP logout complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: $e")
        }
    }

    override fun makeCall(sipUri: String) {
        Log.i(TAG, "Making call to: $sipUri")

        val account = core.defaultAccount
        if (account == null || account.state != LinphoneRegistrationState.Ok) {
            Log.e(TAG, "Cannot make call: account not registered")
            return
        }

        if (core.callsNb > 0) {
            Log.w(TAG, "Call already in progress")
            return
        }

        val factory = Factory.instance()
        val remoteAddress = factory.createAddress(sipUri)

        if (remoteAddress == null) {
            Log.e(TAG, "Cannot create address: $sipUri")
            return
        }

        val params = core.createCallParams(null)
        if (params == null) {
            Log.e(TAG, "Cannot create call params")
            return
        }

        // Video configuration
        params.mediaEncryption = MediaEncryption.None
        params.isVideoEnabled = true
        params.isEarlyMediaSendingEnabled = true
        params.videoDirection = MediaDirection.SendRecv

        try {
            core.inviteAddressWithParams(remoteAddress, params)
            Log.i(TAG, "Call initiated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error making call: $e")
        }
    }

    override fun answerCall() {
        Log.i(TAG, "Answering call")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                val params = core.createCallParams(it)
                params?.isVideoEnabled = it.remoteParams?.isVideoEnabled == true
                it.acceptWithParams(params)
                Log.i(TAG, "Call accepted")
            } catch (e: Exception) {
                Log.e(TAG, "Error accepting call: $e")
                try {
                    it.accept()
                } catch (e2: Exception) {
                    Log.e(TAG, "Fallback accept failed: $e2")
                }
            }
        } ?: Log.w(TAG, "No call to accept")
    }

    override fun hangUp() {
        Log.i(TAG, "Hanging up")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                when (it.state) {
                    Call.State.IncomingReceived,
                    Call.State.IncomingEarlyMedia -> {
                        it.decline(Reason.Declined)
                    }
                    else -> {
                        it.terminate()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hanging up: $e")
            }
        } ?: Log.w(TAG, "No call to terminate")
    }

    override fun toggleVideo() {
        if (core.callsNb == 0) return

        val call = core.currentCall ?: core.calls[0]
        call ?: return

        val currentVideoState = call.currentParams.isVideoEnabled
        val params = core.createCallParams(call)
        params?.isVideoEnabled = !currentVideoState
        call.update(params)

        _callState.update { it.copy(isVideoEnabled = !currentVideoState) }
    }

    override fun toggleCamera() {
        val currentDevice = core.videoDevice
        for (camera in core.videoDevicesList) {
            if (camera != currentDevice && camera != "StaticImage: Static picture") {
                core.videoDevice = camera
                _callState.update { it.copy(isCameraFront = !it.isCameraFront) }
                break
            }
        }
    }

    override fun pauseOrResume() {
        if (core.callsNb == 0) return
        val call = core.currentCall ?: core.calls[0]
        call ?: return

        if (call.state != Call.State.Paused && call.state != Call.State.Pausing) {
            call.pause()
        } else if (call.state != Call.State.Resuming) {
            call.resume()
        }
    }

    override fun toggleMute() {
        core.isMicEnabled = !core.isMicEnabled
        _callState.update { it.copy(isAudioMuted = !core.isMicEnabled) }
    }

    override fun toggleSpeaker() {
        // Speaker is handled by Android audio manager - this is a placeholder
        Log.i(TAG, "Toggle speaker - needs Android AudioManager")
    }

    override fun isInitialized(): Boolean = isInitialized

    override fun isRegistered(): Boolean =
        core.defaultAccount?.state == LinphoneRegistrationState.Ok

    override fun startBackgroundService() {
        try {
            val serviceIntent = Intent(context, CallService::class.java).apply {
                action = CallService.ACTION_START_CALL_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.i(TAG, "Background service started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting background service: $e")
        }
    }

    override fun stopBackgroundService() {
        try {
            val serviceIntent = Intent(context, CallService::class.java)
            context.stopService(serviceIntent)
            Log.i(TAG, "Background service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping background service: $e")
        }
    }

    override fun destroy() {
        Log.i(TAG, "Destroying VoIP service")
        try {
            core.removeListener(coreListener)
            core.stop()
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying service: $e")
        }
    }

    /**
     * Get the Linphone Core instance for advanced operations
     */
    fun getCore(): Core = core

    private fun mapLinphoneCallState(state: Call.State?): CallState {
        return when (state) {
            Call.State.Idle -> CallState.IDLE
            Call.State.OutgoingInit -> CallState.OUTGOING_INIT
            Call.State.OutgoingProgress -> CallState.OUTGOING_PROGRESS
            Call.State.OutgoingRinging -> CallState.OUTGOING_RINGING
            Call.State.OutgoingEarlyMedia -> CallState.OUTGOING_EARLY_MEDIA
            Call.State.IncomingReceived -> CallState.INCOMING_RECEIVED
            Call.State.IncomingEarlyMedia -> CallState.INCOMING_EARLY_MEDIA
            Call.State.Connected -> CallState.CONNECTED
            Call.State.StreamsRunning -> CallState.STREAMS_RUNNING
            Call.State.Pausing -> CallState.PAUSING
            Call.State.Paused -> CallState.PAUSED
            Call.State.Resuming -> CallState.RESUMING
            Call.State.Updating -> CallState.UPDATING
            Call.State.UpdatedByRemote -> CallState.UPDATED_BY_REMOTE
            Call.State.Error -> CallState.ERROR
            Call.State.End -> CallState.END
            Call.State.Released -> CallState.RELEASED
            else -> CallState.IDLE
        }
    }
}
