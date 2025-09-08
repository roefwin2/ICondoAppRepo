package com.example.voip.voip.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.TextureView
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import com.example.voip.voip.core.notification.CallService
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.LogCollectionState
import org.linphone.core.MediaEncryption
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.mediastream.video.capture.CaptureTextureView
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.domain.models.ICondoCall
import com.example.voip.voip.presenter.call.activities.CallingActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.linphone.core.AudioDevice
import org.linphone.core.ConsolidatedPresence
import org.linphone.core.GlobalState
import org.linphone.core.Reason
import org.linphone.core.tools.Log

class ICondoLinphoneImpl(private val context: Context) : ICondoVoip {
    private val TAG = "ICondoLinphoneImpl"
    internal lateinit var core: Core

    // Exposer le core pour le service
    fun getCore(): Core = core

    private val _accountState: MutableStateFlow<AccountState?> = MutableStateFlow<AccountState?>(null)
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
            Log.i(TAG, "Call state changed: ${state?.name} - $message")

            // Log détaillé pour le debugging
            when(state) {
                Call.State.Error -> {
                    Log.e(TAG, "Call error details: $message")
                    val errorInfo = call.errorInfo
                    Log.e(TAG, "Error info - Reason: ${errorInfo.reason}, Protocol code: ${errorInfo.protocolCode}")
                }
                Call.State.Released -> {
                    Log.i(TAG, "Call released")
                }
                else -> {
                    Log.i(TAG, "Call state: ${state?.name}")
                }
            }

            _callState.update {
                ICondoCall(call = call, state = state ?: Call.State.Idle)
            }
        }
    }

    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, context)

        // Configuration réseau améliorée
        core.isPushNotificationEnabled = true
        core.enableLogCollection(LogCollectionState.Enabled)

        // Configuration réseau pour éviter les problèmes IPv6/IPv4
        core.isIpv6Enabled = false // Forcer IPv4 si votre serveur est en IPv4
        core.setUserAgent("ICondoApp", "1.0")

        // Configuration audio/vidéo par défaut
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true

        // Timeouts et retry
        core.incTimeout = 60000 // 60 secondes pour les appels entrants

        // Ajouter le listener dès l'init
        core.addListener(coreListener)
    }

    override fun login(
        username: String,
        password: String,
        domain: String,
        transportType: TransportType
    ) {
        Log.i(TAG, "Connexion: $username@$domain avec transport: ${transportType.name}")

        val factory = Factory.instance()
        val identityUri = "sip:$username@$domain"

        // Nettoyage comme dans votre code original
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
        params.isOutboundProxyEnabled = true // Activé pour améliorer la connectivité

        // Configuration réseau avancée
        params.natPolicy = core.createNatPolicy()
        params.natPolicy?.isStunEnabled = true
        params.natPolicy?.isIceEnabled = true

        val account = core.createAccount(params)

        // Créer et ajouter le compte
        core.clearAccounts()
        core.addAccount(account)
        core.defaultAccount = account

        // Démarrer le core si nécessaire
        if (core.globalState == GlobalState.Ready || core.globalState == GlobalState.Off) {
            try {
                core.start()
                Log.i(TAG, "Core démarré avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du démarrage du core: $e")
            }
        }
    }

    override fun outgoingCall(remoteSipUri: String) {
        Log.i(TAG, "Tentative d'appel sortant vers: $remoteSipUri")

        // Vérifier que nous sommes bien connectés
        val account = core.defaultAccount
        if (account == null || account.state != RegistrationState.Ok) {
            Log.e(TAG, "Impossible d'effectuer l'appel: compte non enregistré")
            return
        }

        // Vérifier qu'il n'y a pas déjà un appel en cours
        if (core.callsNb > 0) {
            Log.w(TAG, "Un appel est déjà en cours")
            return
        }

        val factory = Factory.instance()
        val remoteAddress = factory.createAddress(remoteSipUri)

        if (remoteAddress == null) {
            Log.e(TAG, "Impossible de créer l'adresse: $remoteSipUri")
            return
        }

        Log.i(TAG, "Adresse distante créée: ${remoteAddress.asStringUriOnly()}")

        // Créer les paramètres d'appel
        val params = core.createCallParams(null)
        if (params == null) {
            Log.e(TAG, "Impossible de créer les paramètres d'appel")
            return
        }

        // Configuration des paramètres
        params.mediaEncryption = MediaEncryption.None
        params.isVideoEnabled = false // Commencer en audio seulement
        params.isEarlyMediaSendingEnabled = true

        // Configuration réseau pour l'appel
        params.isAudioMulticastEnabled = false
        params.isVideoMulticastEnabled = false

        try {
            // Utiliser inviteAddressWithParams au lieu de inviteAddress
            val call = core.inviteAddressWithParams(remoteAddress, params)
            if (call != null) {
                val callActivityIntent = Intent(context, CallingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("incoming_call", true)
                    putExtra("caller_name", "callerName")
                    putExtra("caller_number", "phoneNumber")
                }
                startActivity(context,callActivityIntent,null)
                Log.i(TAG, "Appel initié avec succès")
            } else {
                Log.e(TAG, "Échec de l'initiation de l'appel")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'appel: ${e.message}", e)
        }
    }

    override fun answerCall() {
        Log.i(TAG, "Réponse à l'appel")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                // Créer des paramètres de réponse
                val params = core.createCallParams(it)
                params?.isVideoEnabled = false // Audio seulement pour commencer

                it.acceptWithParams(params)
                Log.i(TAG, "Appel accepté avec paramètres")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'acceptation: $e")
                // Fallback sur accept() simple
                try {
                    it.accept()
                } catch (e2: Exception) {
                    Log.e(TAG, "Erreur fallback accept(): $e2")
                }
            }
        } ?: Log.w(TAG, "Aucun appel à accepter")
    }

    override fun hangUp() {
        Log.i(TAG, "Raccrochage")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                when(it.state) {
                    Call.State.IncomingReceived,
                    Call.State.IncomingEarlyMedia -> {
                        it.decline(Reason.Declined)
                        Log.i(TAG, "Appel entrant refusé")
                    }
                    else -> {
                        it.terminate()
                        Log.i(TAG, "Appel terminé")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du raccrochage: $e")
            }
        } ?: Log.w(TAG, "Aucun appel à terminer")
    }

    // Méthode utilitaire pour diagnostiquer les problèmes réseau
    fun diagnoseNetworkIssues() {
        Log.i(TAG, "=== Diagnostic réseau ===")
        Log.i(TAG, "IPv6 enabled: ${core.isIpv6Enabled}")
        Log.i(TAG, "Current calls: ${core.callsNb}")

        core.defaultAccount?.let { account ->
            Log.i(TAG, "Account state: ${account.state}")
            Log.i(TAG, "Identity: ${account.params.identityAddress?.asStringUriOnly()}")
            Log.i(TAG, "Server: ${account.params.serverAddress?.asStringUriOnly()}")
            Log.i(TAG, "Transport: ${account.params.serverAddress?.transport}")
        } ?: Log.w(TAG, "No default account")

        // Vérifier les codecs audio disponibles
        Log.i(TAG, "Audio codecs:")
        core.audioPayloadTypes.forEach { codec ->
            Log.i(TAG, "  - ${codec.mimeType}: enabled=${codec.enabled()}")
        }
    }

    // Reste de vos méthodes existantes...
    override fun initVideo(textureView: TextureView, captureTextureView: CaptureTextureView) {
        core.nativeVideoWindowId = textureView
        core.nativePreviewWindowId = captureTextureView
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true
        core.videoActivationPolicy.automaticallyAccept = true
    }

    override fun toggleVideo() {
        if (core.callsNb == 0) return
        val call = core.currentCall ?: core.calls[0]
        call ?: return

        val params = core.createCallParams(call)
        params?.isVideoEnabled = !call.currentParams.isVideoEnabled
        call.update(params)
    }

    override fun toggleCamera() {
        val currentDevice = core.videoDevice
        for (camera in core.videoDevicesList) {
            if (camera != currentDevice && camera != "StaticImage: Static picture") {
                core.videoDevice = camera
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

    override fun startKeepAliveService() {
        val serviceIntent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_START_CALL_SERVICE
        }
        Log.i(TAG, "Démarrage du service keep-alive")
        try {
            // Utiliser startForegroundService pour Android 8+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.i(TAG, "Service keep-alive démarré avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du démarrage du service: $e")
        }
    }

    override fun logout() {
        Log.i(TAG, "Déconnexion")
        val serviceIntent = Intent(context, CallService::class.java)
        context.stopService(serviceIntent)

        // Supprimer le listener avant d'arrêter le core
        core.removeListener(coreListener)
        core.stop()
    }
}

data class AccountState(
    val message: String,
    val registrationState: RegistrationState?
)