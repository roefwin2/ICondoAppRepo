package com.example.voip.voip.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.TextureView
import com.example.voip.voip.core.notification.CallService
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.domain.VoipEventHandler
import com.example.voip.voip.domain.models.ICondoCall
import com.example.voip.voip.presenter.call.activities.CallingActivity
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log
import org.linphone.mediastream.video.capture.CaptureTextureView

class ICondoLinphoneImpl(private val context: Context,private val voipEventHandler: VoipEventHandler? = null) : ICondoVoip {
    private val TAG = "[ICONDO_VIDEO]"
    internal lateinit var core: Core

    fun getCore(): Core = core

    private val _accountState: MutableStateFlow<AccountState?> =
        MutableStateFlow<AccountState?>(null)
    override val accountState = _accountState.asStateFlow()

    private val _callState: MutableStateFlow<ICondoCall> = MutableStateFlow(ICondoCall())
    override val callState = _callState.asStateFlow()

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            Log.i(TAG, "Account registration state: ${state?.name} - $message")
            _accountState.update {
                AccountState(message = message, registrationState = state)
            }

            if (core.globalState == GlobalState.Off) return

            if (state == RegistrationState.Ok) {
                core.consolidatedPresence = ConsolidatedPresence.Online
                Log.i(TAG, "Account successfully registered: [${account.params.identityAddress?.asStringUriOnly()}]")
                logVideoCapabilities()
                startKeepAliveService()
            } else if (state == RegistrationState.Failed) {
                Log.e(TAG, "Registration failed: $message")
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            Log.i(TAG, "========================================")
            Log.i(TAG, "Call state changed: ${state?.name} - $message")

            // Logs détaillés sur la vidéo
            logCallVideoStatus(call, state)

            when (state) {
                Call.State.IncomingReceived -> {
                    Log.i(TAG, "📞 INCOMING CALL")
                    Log.i(TAG, "Remote wants video: ${call.remoteParams?.isVideoEnabled}")
                    Log.i(TAG, "Current params video: ${call.currentParams?.isVideoEnabled}")
                }

                Call.State.OutgoingInit -> {
                    Log.i(TAG, "📞 OUTGOING CALL INIT")
                    Log.i(TAG, "Video enabled in params: ${call.params?.isVideoEnabled}")
                }

                Call.State.Connected -> {
                    Log.i(TAG, "✅ CALL CONNECTED")
                    logCallVideoStatus(call, state)
                }

                Call.State.StreamsRunning -> {
                    Log.i(TAG, "🎥 STREAMS RUNNING")
                    logCallVideoStatus(call, state)
                    logVideoStreamDetails(call)
                    // Forcer l'activation si nécessaire
                    if (call.params.isVideoEnabled == true && !call.currentParams.isVideoEnabled) {
                        val params = core.createCallParams(call)
                        params?.isVideoEnabled = true
                        call.update(params)
                    }

                }

                Call.State.UpdatedByRemote -> {
                    Log.i(TAG, "🔄 CALL UPDATED BY REMOTE")
                    logCallVideoStatus(call, state)
                }

                Call.State.Error -> {
                    Log.e(TAG, "❌ Call error: $message")
                    val errorInfo = call.errorInfo
                    Log.e(TAG, "Error info - Reason: ${errorInfo.reason}, Protocol code: ${errorInfo.protocolCode}")
                }

                Call.State.Released -> {
                    Log.i(TAG, "📴 Call released")
                }

                else -> {
                    Log.i(TAG, "Call state: ${state?.name}")
                }
            }
            Log.i(TAG, "========================================")

            _callState.update {
                ICondoCall(call = call, state = state ?: Call.State.Idle)
            }
        }
    }

    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "ICondoVideo")
        core = factory.createCore(null, null, context)

        Log.i(TAG, "🎬 Initializing Linphone Core with video support")

        // Configuration réseau
        core.isPushNotificationEnabled = true
        core.enableLogCollection(LogCollectionState.Enabled)
        core.isIpv6Enabled = false
        core.setUserAgent("ICondoApp", "1.0")

        // ✅ Configuration vidéo CRITIQUE
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true
        core.videoActivationPolicy.automaticallyAccept = true
        core.videoActivationPolicy.automaticallyInitiate = true

        Log.i(TAG, "Video capture enabled: ${core.isVideoCaptureEnabled}")
        Log.i(TAG, "Video display enabled: ${core.isVideoDisplayEnabled}")
        Log.i(TAG, "Video auto-accept: ${core.videoActivationPolicy.automaticallyAccept}")
        Log.i(TAG, "Video auto-initiate: ${core.videoActivationPolicy.automaticallyInitiate}")

        // Timeouts
        core.incTimeout = 60000

        // Prioriser les codecs vidéo supportés
        core.videoPayloadTypes.forEach { codec ->
            when(codec.mimeType) {
                "VP8", "H264" -> codec.enable(true)
                else -> codec.enable(false)
            }
        }

        // Forcer l'envoi vidéo même sans négociation
        core.isVideoAdaptiveJittcompEnabled = true

        // Ajouter le listener
        core.addListener(coreListener)

        // Log des codecs disponibles
        logAvailableCodecs()
    }

    override fun login(
        username: String,
        password: String,
        domain: String,
        transportType: TransportType
    ) {
        Log.i(TAG, "🔐 Login: $username@$domain avec transport: ${transportType.name}")

        val factory = Factory.instance()
        val identityUri = "sip:$username@$domain"

        // Nettoyage
        core.accountList
            .filter { it.params.identityAddress?.asStringUriOnly() == identityUri }
            .forEach { core.removeAccount(it) }

        core.authInfoList
            .filter { it.username == username && (it.domain == domain || it.realm == domain) }
            .forEach { core.removeAuthInfo(it) }

        // AuthInfo
        val auth = factory.createAuthInfo(username, null, password, null, null, domain, null)
        core.addAuthInfo(auth)

        // Paramètres de compte
        val params = core.createAccountParams()
        params.identityAddress = factory.createAddress(identityUri)

        val server = factory.createAddress("sip:$domain")!!.apply {
            transport = transportType
            port = when (transportType) {
                TransportType.Tls -> 5061
                TransportType.Tcp, TransportType.Udp -> 5060
                else -> 5060
            }
        }

        params.serverAddress = server
        params.expires = 300
        params.isRegisterEnabled = true
        params.isOutboundProxyEnabled = true

        // Configuration NAT pour la vidéo
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
                Log.i(TAG, "✅ Core started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting core: $e")
            }
        }
    }

    override fun outgoingCall(remoteSipUri: String) {
        Log.i(TAG, "📞 OUTGOING VIDEO CALL to: $remoteSipUri")

        val account = core.defaultAccount
        if (account == null || account.state != RegistrationState.Ok) {
            Log.e(TAG, "❌ Cannot make call: account not registered")
            return
        }

        if (core.callsNb > 0) {
            Log.w(TAG, "⚠️ Call already in progress")
            return
        }

        val factory = Factory.instance()
        val remoteAddress = factory.createAddress(remoteSipUri)

        if (remoteAddress == null) {
            Log.e(TAG, "❌ Cannot create address: $remoteSipUri")
            return
        }

        Log.i(TAG, "Remote address: ${remoteAddress.asStringUriOnly()}")

        val params = core.createCallParams(null)
        if (params == null) {
            Log.e(TAG, "❌ Cannot create call params")
            return
        }

        // ✅ Configuration VIDÉO pour l'appel sortant
        params.mediaEncryption = MediaEncryption.None
        params.isVideoEnabled = true  // ACTIVER LA VIDÉO
        params.isEarlyMediaSendingEnabled = true
        params.isAudioMulticastEnabled = false
        params.isVideoMulticastEnabled = false
        params.videoDirection = MediaDirection.SendRecv

        Log.i(TAG, "🎥 Call params configured:")
        Log.i(TAG, "  - Video enabled: ${params.isVideoEnabled}")
        Log.i(TAG, "  - Audio enabled: ${params.isAudioEnabled}")
        Log.i(TAG, "  - Media encryption: ${params.mediaEncryption}")

        try {
            val call = core.inviteAddressWithParams(remoteAddress, params)
            if (call != null) {
                Log.i(TAG, "✅ Call initiated successfully")
                Log.i(TAG, "Call video enabled: ${call.params?.isVideoEnabled}")

                val callActivityIntent = Intent(context, CallingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("incoming_call", false)
                    putExtra("caller_name", "Video Call")
                    putExtra("caller_number", remoteSipUri)
                }
                context.startActivity(callActivityIntent)
            } else {
                Log.e(TAG, "❌ Failed to initiate call")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during call: ${e.message}", e)
        }
    }

    override fun answerCall() {
        Log.i(TAG, "📞 ANSWERING CALL")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                Log.i(TAG, "Incoming call params:")
                Log.i(TAG, "  - Remote video enabled: ${it.remoteParams?.isVideoEnabled}")
                Log.i(TAG, "  - Current video enabled: ${it.currentParams?.isVideoEnabled}")

                // ✅ CORRECTION CRITIQUE: Accepter avec vidéo si disponible
                val params = core.createCallParams(it)

                // Conserver la vidéo si le remote l'a demandée
                val shouldEnableVideo = it.remoteParams?.isVideoEnabled == true
                params?.isVideoEnabled = shouldEnableVideo

                Log.i(TAG, "🎥 Accepting call with video: $shouldEnableVideo")

                it.acceptWithParams(params)
                Log.i(TAG, "✅ Call accepted with params")

                // Vérifier après acceptation
                Log.i(TAG, "After accept - video enabled: ${it.currentParams?.isVideoEnabled}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error accepting call: $e")
                try {
                    it.accept()
                    Log.i(TAG, "✅ Call accepted (fallback)")
                } catch (e2: Exception) {
                    Log.e(TAG, "❌ Fallback accept failed: $e2")
                }
            }
        } ?: Log.w(TAG, "⚠️ No call to accept")
    }

    override fun hangUp() {
        Log.i(TAG, "📴 HANGING UP")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                when (it.state) {
                    Call.State.IncomingReceived,
                    Call.State.IncomingEarlyMedia -> {
                        it.decline(Reason.Declined)
                        Log.i(TAG, "Call declined")
                    }
                    else -> {
                        it.terminate()
                        Log.i(TAG, "Call terminated")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error hanging up: $e")
            }
        } ?: Log.w(TAG, "⚠️ No call to terminate")
    }

    override fun initVideo(textureView: TextureView, captureTextureView: CaptureTextureView) {
        Log.i(TAG, "🎥 INITIALIZING VIDEO VIEWS")

        core.nativeVideoWindowId = textureView
        core.nativePreviewWindowId = captureTextureView
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true
        core.videoActivationPolicy.automaticallyAccept = true

        Log.i(TAG, "Video window ID set: $textureView")
        Log.i(TAG, "Preview window ID set: $captureTextureView")
        Log.i(TAG, "Current video device: ${core.videoDevice}")

        // Vérifier les caméras disponibles
        Log.i(TAG, "Available cameras:")
        core.videoDevicesList.forEach { camera ->
            Log.i(TAG, "  - $camera")
        }
    }

    override fun toggleVideo() {
        Log.i(TAG, "🔄 TOGGLING VIDEO")
        if (core.callsNb == 0) {
            Log.w(TAG, "⚠️ No active call")
            return
        }

        val call = core.currentCall ?: core.calls[0]
        call ?: return

        val currentVideoState = call.currentParams.isVideoEnabled
        Log.i(TAG, "Current video state: $currentVideoState")

        val params = core.createCallParams(call)
        params?.isVideoEnabled = !currentVideoState

        Log.i(TAG, "Setting video to: ${params?.isVideoEnabled}")
        call.update(params)
    }

    override fun toggleCamera() {
        Log.i(TAG, "🔄 SWITCHING CAMERA")
        val currentDevice = core.videoDevice
        Log.i(TAG, "Current camera: $currentDevice")

        for (camera in core.videoDevicesList) {
            if (camera != currentDevice && camera != "StaticImage: Static picture") {
                core.videoDevice = camera
                Log.i(TAG, "Switched to camera: $camera")
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
            Log.i(TAG, "⏸️ Call paused")
        } else if (call.state != Call.State.Resuming) {
            call.resume()
            Log.i(TAG, "▶️ Call resumed")
        }
    }

    // ✅ MÉTHODES DE DIAGNOSTIC
    private fun logCallVideoStatus(call: Call, state: Call.State?) {
        Log.i(TAG, "📊 Video status for state ${state?.name}:")
        Log.i(TAG, "  - Current params video: ${call.currentParams?.isVideoEnabled}")
        Log.i(TAG, "  - Remote params video: ${call.remoteParams?.isVideoEnabled}")
        Log.i(TAG, "  - Params video: ${call.params?.isVideoEnabled}")
        Log.i(TAG, "  - Video capture enabled: ${core.isVideoCaptureEnabled}")
        Log.i(TAG, "  - Video display enabled: ${core.isVideoDisplayEnabled}")
        Log.i(TAG, "  - Video device: ${core.videoDevice}")
    }

    private fun logVideoStreamDetails(call: Call) {
        Log.i(TAG, "🎞️ Video stream details:")
        val audioStats = call.audioStats
        val videoStats = call.videoStats

        Log.i(TAG, "Audio stats:")
        Log.i(TAG, "  - Download bandwidth: ${audioStats?.downloadBandwidth}")
        Log.i(TAG, "  - Upload bandwidth: ${audioStats?.uploadBandwidth}")

        if (videoStats != null) {
            Log.i(TAG, "Video stats:")
            Log.i(TAG, "  - Download bandwidth: ${videoStats.downloadBandwidth}")
            Log.i(TAG, "  - Upload bandwidth: ${videoStats.uploadBandwidth}")
            Log.i(TAG, "  - Received framerate: ${videoStats.receiverLossRate}")
            Log.i(TAG, "  - Sent framerate: ${videoStats.rtpSent}")
        } else {
            Log.w(TAG, "⚠️ No video stats available")
        }
    }

    private fun logVideoCapabilities() {
        Log.i(TAG, "🎥 Video capabilities:")
        Log.i(TAG, "Video codecs:")
        core.videoPayloadTypes.forEach { codec ->
            Log.i(TAG, "  - ${codec.mimeType}: enabled=${codec.enabled()}, recv_fmtp=${codec.recvFmtp}")
        }
    }

    private fun logAvailableCodecs() {
        Log.i(TAG, "🔊 Audio codecs:")
        core.audioPayloadTypes.forEach { codec ->
            Log.i(TAG, "  - ${codec.mimeType}: enabled=${codec.enabled()}")
        }

        Log.i(TAG, "🎥 Video codecs:")
        core.videoPayloadTypes.forEach { codec ->
            Log.i(TAG, "  - ${codec.mimeType}: enabled=${codec.enabled()}")
        }
    }

    fun diagnoseNetworkIssues() {
        Log.i(TAG, "=== NETWORK DIAGNOSTIC ===")
        Log.i(TAG, "IPv6 enabled: ${core.isIpv6Enabled}")
        Log.i(TAG, "Current calls: ${core.callsNb}")
        Log.i(TAG, "Video capture enabled: ${core.isVideoCaptureEnabled}")
        Log.i(TAG, "Video display enabled: ${core.isVideoDisplayEnabled}")

        core.defaultAccount?.let { account ->
            Log.i(TAG, "Account state: ${account.state}")
            Log.i(TAG, "Identity: ${account.params.identityAddress?.asStringUriOnly()}")
            Log.i(TAG, "Server: ${account.params.serverAddress?.asStringUriOnly()}")
            Log.i(TAG, "Transport: ${account.params.serverAddress?.transport}")
        } ?: Log.w(TAG, "No default account")

        logAvailableCodecs()
    }

    override fun startKeepAliveService() {
        val serviceIntent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_START_CALL_SERVICE
        }
        Log.i(TAG, "Starting keep-alive service")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.i(TAG, "✅ Keep-alive service started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting service: $e")
        }
    }

    fun requestDoorOpen() {
        Log.i(TAG, "🚪 Door open requested from call")
        voipEventHandler?.onDoorOpenRequested()
    }

    override fun logout() {
        Log.i(TAG, "📴 ═══════════════════════════════════════")
        Log.i(TAG, "📴 STARTING VOIP LOGOUT (keeping core alive)")
        Log.i(TAG, "📴 ═══════════════════════════════════════")

        try {
            // ═══ ÉTAPE 1: Terminer les appels en cours ═══
            Log.i(TAG, "📴 Step 1: Terminating active calls")
            core.calls.forEach { call ->
                try {
                    Log.i(TAG, "  📞 Terminating call: ${call.remoteAddress?.asStringUriOnly()}")
                    call.terminate()
                } catch (e: Exception) {
                    Log.e(TAG, "  ❌ Error terminating call: $e")
                }
            }

            // ═══ ÉTAPE 2: Se désenregistrer du serveur SIP ═══
            Log.i(TAG, "📴 Step 2: Unregistering from SIP server")
            core.defaultAccount?.let { account ->
                try {
                    val params = account.params.clone()
                    params?.isRegisterEnabled = false
                    account.params = params
                    Log.i(TAG, "  ✅ Unregister request sent")
                } catch (e: Exception) {
                    Log.e(TAG, "  ❌ Error unregistering: $e")
                }
            }

            // ═══ ÉTAPE 3: Arrêter le service foreground ═══
            Log.i(TAG, "📴 Step 3: Stopping foreground service")
            stopKeepAliveService()

            // ═══ ÉTAPE 4: Nettoyer les comptes et auth (SANS arrêter le core) ═══
            Log.i(TAG, "📴 Step 4: Clearing accounts and auth info")
            try {
                core.clearAccounts()
                core.clearAllAuthInfo()
                Log.i(TAG, "  ✅ Accounts and auth cleared")
            } catch (e: Exception) {
                Log.e(TAG, "  ❌ Error clearing accounts: $e")
            }

            // ═══ ÉTAPE 5: Réinitialiser les états ═══
            Log.i(TAG, "📴 Step 5: Resetting state flows")
            _accountState.value = null
            _callState.value = ICondoCall()

            // ❌ NE PAS FAIRE: core.stop() - cela détruit le core
            // ❌ NE PAS FAIRE: core.removeListener(coreListener) - on en a besoin pour le prochain login

            Log.i(TAG, "📴 ═══════════════════════════════════════")
            Log.i(TAG, "📴 VOIP LOGOUT COMPLETE (core still alive) ✅")
            Log.i(TAG, "📴 ═══════════════════════════════════════")

        } catch (e: Exception) {
            Log.e(TAG, "📴 ❌ CRITICAL ERROR during logout: ${e.message}")
            e.printStackTrace()

            // Fallback: nettoyer ce qu'on peut
            try {
                stopKeepAliveService()
                core.clearAccounts()
                core.clearAllAuthInfo()
            } catch (_: Exception) {}

            _accountState.value = null
            _callState.value = ICondoCall()
        }
    }

    private fun stopKeepAliveService() {
        try {
            val serviceIntent = Intent(context, CallService::class.java)
            context.stopService(serviceIntent)
            Log.i(TAG, "  ✅ Keep-alive service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "  ❌ Error stopping service: $e")
        }
    }

}

data class AccountState(
    val message: String,
    val registrationState: RegistrationState?
)