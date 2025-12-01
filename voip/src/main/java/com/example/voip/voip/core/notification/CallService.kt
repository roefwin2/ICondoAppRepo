package com.example.voip.voip.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.voip.R
import com.example.voip.voip.data.ICondoLinphoneImpl
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.presenter.call.activities.CallingActivity
import org.koin.android.ext.android.inject
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.RegistrationState
import org.linphone.core.tools.Log
import org.linphone.core.tools.service.CoreService

class CallService : CoreService() {
    private val TAG = "CallService"
    private val iCondoVoip: ICondoVoip by inject()

    private val channelId = "incoming_call_channel"
    private val keepAliveChannelId = "voip_service_channel"
    private val inCallChannelId = "in_call_channel"
    private val notificationId = 1000

    private var isInForeground = false

    private val coreListener = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            Log.i(TAG, "📞 Call state: ${state?.name}")

            when (state) {
                Call.State.IncomingReceived -> {
                    showIncomingCallNotification(call)
                }

                Call.State.Connected, Call.State.StreamsRunning -> {
                    showInCallNotification(call)
                }

                Call.State.Released, Call.State.End, Call.State.Error -> {
                    backToKeepAliveMode()
                }

                else -> {
                    Log.d(TAG, "État: ${state?.name}")
                }
            }
        }

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            Log.i(TAG, "🔐 Registration: ${state?.name}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "🚀 Service démarré")

        createAllNotificationChannels()
        startKeepAliveForegroundImmediately()

        try {
            val core = (iCondoVoip as ICondoLinphoneImpl).getCore()
            core.addListener(coreListener)
            Log.i(TAG, "✅ Listener configuré")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur listener: $e")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "📨 Action: ${intent?.action}")

        if (!isInForeground) {
            startKeepAliveForegroundImmediately()
        }

        when (intent?.action) {
            ACTION_START_CALL_SERVICE -> {
                Log.i(TAG, "Keep-alive activé")
            }

            ACTION_ANSWER_CALL -> {
                handleAnswerCall()
            }

            ACTION_DECLINE_CALL -> {
                handleDeclineCall()
            }

            ACTION_LOGOUT -> {
                handleLogout()
            }

            null -> {
                Log.i(TAG, "Service auto-démarré")
            }
        }

        return START_STICKY
    }

    /**
     * Gère la déconnexion complète du service
     */
    private fun handleLogout() {
        Log.i(TAG, "📴 Handling logout - stopping service")

        try {
            // Terminer tout appel en cours
            val core = (iCondoVoip as? ICondoLinphoneImpl)?.getCore()
            core?.currentCall?.terminate()

            // Supprimer le listener
            core?.removeListener(coreListener)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during logout cleanup: $e")
        }

        // Arrêter le service
        stopForeground(STOP_FOREGROUND_REMOVE)
        isInForeground = false
        stopSelf()

        Log.i(TAG, "✅ Service stopped for logout")
    }

    private fun createAllNotificationChannels() {
        Log.i(TAG, "📢 Création des canaux de notification")

        // Canal appels entrants - Haute priorité
        createNotificationChannel(
            channelId = channelId,
            name = "Appels entrants",
            description = "Notifications pour les appels VoIP entrants",
            importance = NotificationManager.IMPORTANCE_HIGH,
            enableVibration = true,
            enableSound = false
        )

        // Canal appel en cours - Priorité normale
        createNotificationChannel(
            channelId = inCallChannelId,
            name = "Appel en cours",
            description = "Notification affichée pendant un appel",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            enableVibration = false,
            enableSound = false
        )

        // Canal service - Basse priorité
        createNotificationChannel(
            channelId = keepAliveChannelId,
            name = "Service VoIP",
            description = "Service d'écoute pour les appels entrants",
            importance = NotificationManager.IMPORTANCE_LOW,
            enableVibration = false,
            enableSound = false
        )
    }

    private fun createNotificationChannel(
        channelId: String,
        name: String,
        description: String,
        importance: Int,
        enableVibration: Boolean,
        enableSound: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
                this.enableVibration(enableVibration)
                if (enableVibration) {
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                }
                if (!enableSound) {
                    setSound(null, null)
                }
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i(TAG, "✅ Canal créé: $name")
        }
    }

    private fun startKeepAliveForegroundImmediately() {
        if (isInForeground) {
            Log.d(TAG, "⏭️ Déjà en foreground")
            return
        }

        Log.i(TAG, "🟢 Démarrage service foreground")

        val notification = NotificationCompat.Builder(this, keepAliveChannelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Service d'appels")
            .setContentText("Prêt à recevoir des appels")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(false)
            .setSilent(true)
            .build()

        try {
            startForeground(KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID, notification)
            isInForeground = true
            Log.i(TAG, "✅ Service foreground actif")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur foreground: $e")
        }
    }

    private fun showIncomingCallNotification(call: Call) {
        Log.i(TAG, "📞 Notification appel entrant")

        val callerName = call.remoteAddress?.displayName
            ?: call.remoteAddress?.username
            ?: "Inconnu"

        val phoneNumber = formatPhoneNumber(
            call.remoteAddress?.username
                ?: call.remoteAddress?.asStringUriOnly()
                ?: ""
        )

        // Intent pour répondre
        val answerIntent = Intent(this, CallingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("incoming_call", true)
            putExtra("caller_name", callerName)
            putExtra("caller_number", phoneNumber)
            putExtra("answer", true)
            action = "ANSWER_CALL"
        }

        val answerPendingIntent = PendingIntent.getActivity(
            this, 101, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent pour refuser
        val declineIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_DECLINE_CALL
        }
        val declinePendingIntent = PendingIntent.getService(
            this, 102, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Appel entrant")
            .setContentText(callerName)
            .setSubText(phoneNumber)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setLights(0xFF00FF00.toInt(), 1000, 1000)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_reject,
                    "Refuser",
                    declinePendingIntent
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_accept,
                    "Répondre",
                    answerPendingIntent
                ).build()
            )
            .setContentIntent(answerPendingIntent)
            .setTimeoutAfter(60000) // 60 secondes
            .setColor(0xFF4CAF50.toInt()) // Vert pour appel entrant
            .build()

        startForeground(2, notification)
        Log.i(TAG, "✅ Notification affichée: $callerName")
    }

    private fun showInCallNotification(call: Call) {
        Log.i(TAG, "📱 Notification appel en cours")

        val callerName = call.remoteAddress?.displayName
            ?: call.remoteAddress?.username
            ?: "En cours"

        val phoneNumber = formatPhoneNumber(
            call.remoteAddress?.username
                ?: call.remoteAddress?.asStringUriOnly()
                ?: ""
        )

        val duration = call.duration // En secondes
        val durationText = formatCallDuration(duration)

        // Intent pour ouvrir l'activité d'appel
        val openCallIntent = Intent(this, CallingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val openCallPendingIntent = PendingIntent.getActivity(
            this, 103, openCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent pour raccrocher
        val hangUpIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_DECLINE_CALL
        }
        val hangUpPendingIntent = PendingIntent.getService(
            this, 104, hangUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, inCallChannelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Appel en cours")
            .setContentText("$callerName${if (phoneNumber.isNotEmpty()) " • $phoneNumber" else ""}")
            .setSubText(durationText)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis() - (duration * 1000))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_reject,
                    "Raccrocher",
                    hangUpPendingIntent
                ).build()
            )
            .setContentIntent(openCallPendingIntent)
            .setColor(0xFF2196F3.toInt()) // Bleu pour appel en cours
            .setSilent(true)
            .build()

        startForeground(notificationId + 1, notification)
        Log.i(TAG, "✅ Notification en cours mise à jour")
    }

    private fun handleAnswerCall() {
        Log.i(TAG, "✅ Réponse à l'appel")

        val callingActivityIntent = Intent(this, CallingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("incoming_call", true)
            putExtra("answer", true)
            action = "ANSWER_CALL"
        }

        try {
            startActivity(callingActivityIntent)
            Log.i(TAG, "📱 Interface d'appel lancée")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lancement interface: $e")
            iCondoVoip.answerCall()
        }
    }

    private fun handleDeclineCall() {
        Log.i(TAG, "❌ Refus de l'appel")
        iCondoVoip.hangUp()
    }

    private fun backToKeepAliveMode() {
        Log.i(TAG, "🔄 Retour au mode veille")
        startKeepAliveForegroundImmediately()
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        // Nettoyer le numéro SIP pour n'afficher que le numéro
        return phoneNumber
            .removePrefix("sip:")
            .substringBefore("@")
            .trim()
    }

    private fun formatCallDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return when {
            minutes > 0 -> "${minutes}m ${remainingSeconds}s"
            else -> "${seconds}s"
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "🛑 Service arrêté")
        isInForeground = false

        try {
            // Vérifier si le core est toujours valide avant de supprimer le listener
            val linphone = iCondoVoip as? ICondoLinphoneImpl
            if (linphone != null) {
                val core = linphone.getCore()
                if (core.globalState != org.linphone.core.GlobalState.Off) {
                    core.removeListener(coreListener)
                    Log.i(TAG, "✅ Listener supprimé")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur suppression listener: $e")
        }

        super.onDestroy()
    }

    // Implémentation des méthodes abstraites de CoreService
    override fun createServiceNotificationChannel() {
        Log.d(TAG, "📢 createServiceNotificationChannel()")
    }

    override fun showForegroundServiceNotification(isVideoCall: Boolean) {
        Log.d(TAG, "📱 showForegroundServiceNotification() - Video: $isVideoCall")
    }

    override fun hideForegroundServiceNotification() {
        Log.d(TAG, "🔇 hideForegroundServiceNotification()")
        backToKeepAliveMode()
    }

    companion object {
        const val ACTION_START_CALL_SERVICE = "com.example.condo.ACTION_START_CALL_SERVICE"
        const val ACTION_ANSWER_CALL = "action_answer_call"
        const val ACTION_DECLINE_CALL = "action_decline_call"
        const val ACTION_LOGOUT = "action_logout"
        const val KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID = 5
    }
}