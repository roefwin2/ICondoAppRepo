package com.example.voip.voip.data

import android.content.Context
import android.content.Intent
import android.view.TextureView
import androidx.annotation.WorkerThread
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.linphone.core.AudioDevice
import org.linphone.core.ConsolidatedPresence
import org.linphone.core.GlobalState
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
                if (!core.isPushNotificationAvailable || !account.params.isPushNotificationAvailable) {

                }
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            Log.i(TAG, "Call state changed: ${state?.name} - $message")
            _callState.update {
                ICondoCall(call = call, state = state ?: Call.State.Idle)
            }
        }
    }

    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, context)

        // Configuration essentielle
        core.isPushNotificationEnabled = true
        core.enableLogCollection(LogCollectionState.Enabled)

        // Ajouter le listener dès l'init
        core.addListener(coreListener)
    }

    override fun login(
        username: String,
        password: String,
        domain: String,
        transportType: TransportType
    ) {
        Log.i(TAG, "Connexion: $username@$domain")

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
        params.setRoutesAddresses(arrayOf(server))
        params.expires = 300
        params.isRegisterEnabled = true

        // Créer et ajouter le compte
        core.clearAccounts()
        val account = core.createAccount(params)
        core.addAccount(account)
        core.defaultAccount = account

        // Démarrer le core
        if (core.globalState == GlobalState.Ready || core.globalState == GlobalState.Off) {
            try {
                core.start()
                Log.i(TAG, "Core démarré avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du démarrage du core: $e")
            }
        }
    }

    override fun answerCall() {
        Log.i(TAG, "Réponse à l'appel")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                it.accept()
                Log.i(TAG, "Appel accepté")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'acceptation: $e")
            }
        } ?: Log.w(TAG, "Aucun appel à accepter")
    }

    override fun hangUp() {
        Log.i(TAG, "Raccrochage")
        val call = core.currentCall ?: core.calls.firstOrNull()
        call?.let {
            try {
                it.terminate()
                Log.i(TAG, "Appel terminé")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du raccrochage: $e")
            }
        } ?: Log.w(TAG, "Aucun appel à terminer")
    }

    // Reste de votre implémentation...
    override fun outgoingCall(remoteSipUri: String) {
        val remoteAddress = Factory.instance().createAddress(remoteSipUri)
        remoteAddress ?: return

        val params = core.createCallParams(null)
        params ?: return

        params.mediaEncryption = MediaEncryption.None
        core.inviteAddressWithParams(remoteAddress, params)
    }

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
            context.startService(serviceIntent)
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