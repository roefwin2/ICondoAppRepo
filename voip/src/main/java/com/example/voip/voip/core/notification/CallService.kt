package com.example.voip.voip.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.MainThread
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
    private val notificationId = 1000

    // Variable pour tracker si on est déjà en foreground
    private var isInForeground = false

    // Listener direct comme dans la démo
    private val coreListener = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            Log.i(TAG, "Call state changed: ${state?.name} - $message")

            when (state) {
                Call.State.IncomingReceived -> {
                    showIncomingCallNotification(call)
                }

                Call.State.Connected -> {
                    showInCallNotification(call)
                }

                Call.State.Released, Call.State.End, Call.State.Error -> {
                    // Retourner au mode keep-alive au lieu de tout arrêter
                    backToKeepAliveMode()
                }

                else -> {
                    Log.d(TAG, "État non géré: ${state?.name}")
                }
            }
        }

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            Log.i(TAG, "Registration state: ${state?.name} - $message")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "CallService created")

        // CRITICAL: Créer les canaux de notification IMMÉDIATEMENT
        createAllNotificationChannels()

        // CRITICAL: Démarrer en foreground IMMÉDIATEMENT pour éviter le crash
        startKeepAliveForegroundImmediately()

        // Ajouter le listener après
        try {
            val core = (iCondoVoip as ICondoLinphoneImpl).getCore()
            core.addListener(coreListener)
            Log.i(TAG, "Listener ajouté au core")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout du listener: $e")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand called with action: ${intent?.action}")

        // CRITICAL: Si on n'est pas encore en foreground, le faire immédiatement
        if (!isInForeground) {
            startKeepAliveForegroundImmediately()
        }

        when (intent?.action) {
            ACTION_START_CALL_SERVICE -> {
                Log.i(TAG, "Keep alive service requested")
                // Déjà géré dans onCreate/onStartCommand
            }

            ACTION_ANSWER_CALL -> {
                handleAnswerCall()
            }

            ACTION_DECLINE_CALL -> {
                handleDeclineCall()
            }

            null -> {
                // Cas où Linphone démarre le service automatiquement
                Log.i(TAG, "Service démarré automatiquement par Linphone")
            }
        }

        return START_STICKY
    }

    private fun createAllNotificationChannels() {
        Log.i(TAG, "Création des canaux de notification")

        // Canal pour les appels entrants (haute priorité)
        createNotificationChannel(
            channelId,
            "Appels entrants",
            NotificationManager.IMPORTANCE_HIGH
        )

        // Canal pour le service keep-alive (basse priorité)
        createNotificationChannel(
            keepAliveChannelId,
            "Service VoIP",
            NotificationManager.IMPORTANCE_LOW
        )
    }

    private fun startKeepAliveForegroundImmediately() {
        if (isInForeground) {
            Log.d(TAG, "Déjà en foreground, skip")
            return
        }

        Log.i(TAG, "Démarrage IMMÉDIAT du service foreground")

        val notification = NotificationCompat.Builder(this, keepAliveChannelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Service VoIP")
            .setContentText("En attente d'appels...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        try {
            startForeground(KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID, notification)
            isInForeground = true
            Log.i(TAG, "Service démarré en foreground avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "ERREUR lors du démarrage foreground: $e")
        }
    }

    private fun showIncomingCallNotification(call: Call) {
        Log.i(TAG, "Affichage notification appel entrant")

        val callerName = call.remoteAddress?.displayName ?: "Appel entrant"
        val phoneNumber = call.remoteAddress?.asStringUriOnly() ?: "Numéro inconnu"

        // Créer les intents pour répondre/refuser
        val answerIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_ANSWER_CALL
        }
        val declineIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_DECLINE_CALL
        }

        val answerPendingIntent = PendingIntent.getService(
            this, 0, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val declinePendingIntent = PendingIntent.getService(
            this, 1, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createIncomingCallNotification(
            this, channelId, callerName, phoneNumber, answerPendingIntent, declinePendingIntent
        )

        // Remplacer la notification keep-alive par celle de l'appel entrant
        startForeground(notificationId, notification)

        // Lancer l'activité d'appel entrant
        val callActivityIntent = Intent(this, CallingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("incoming_call", true)
            putExtra("caller_name", callerName)
            putExtra("caller_number", phoneNumber)
        }
        startActivity(callActivityIntent)
    }

    private fun showInCallNotification(call: Call) {
        Log.i(TAG, "Affichage notification appel en cours")

        val callerName = call.remoteAddress?.displayName ?: "En cours"
        val phoneNumber = call.remoteAddress?.asStringUriOnly() ?: ""

        val hangUpIntent = Intent(this, CallService::class.java).apply {
            action = ACTION_DECLINE_CALL
        }
        val hangUpPendingIntent = PendingIntent.getService(
            this, 2, hangUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Appel en cours")
            .setContentText("$callerName - $phoneNumber")
            .addAction(R.drawable.ic_call_reject, "Raccrocher", hangUpPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        startForeground(notificationId + 1, notification)
    }

    private fun handleAnswerCall() {
        Log.i(TAG, "Réponse à l'appel")
        iCondoVoip.answerCall()
    }

    private fun handleDeclineCall() {
        Log.i(TAG, "Refus de l'appel")
        iCondoVoip.hangUp()
    }

    private fun backToKeepAliveMode() {
        Log.i(TAG, "Retour au mode keep-alive")
        startKeepAliveForegroundImmediately()
    }

    private fun createNotificationChannel(channelId: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance).apply {
                if (importance == NotificationManager.IMPORTANCE_HIGH) {
                    // Pour les appels entrants
                    enableVibration(true)
                    setSound(null, null) // Vous pouvez ajouter un son personnalisé
                } else {
                    // Pour le service keep-alive
                    setSound(null, null)
                    enableVibration(false)
                }
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i(TAG, "Canal de notification créé: $channelId")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "CallService destroyed")
        isInForeground = false

        try {
            val core = (iCondoVoip as ICondoLinphoneImpl).getCore()
            core.removeListener(coreListener)
            Log.i(TAG, "Listener supprimé du core")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression du listener: $e")
        }
        super.onDestroy()
    }

    // Implémentation des méthodes abstraites de CoreService
    override fun createServiceNotificationChannel() {
        // Déjà fait dans createAllNotificationChannels()
        Log.d(TAG, "createServiceNotificationChannel() appelée")
    }

    override fun showForegroundServiceNotification(isVideoCall: Boolean) {
        Log.d(TAG, "showForegroundServiceNotification() appelée - isVideoCall: $isVideoCall")
        // Notre gestion personnalisée dans onCallStateChanged
    }

    override fun hideForegroundServiceNotification() {
        Log.d(TAG, "hideForegroundServiceNotification() appelée")
        backToKeepAliveMode()
    }

    private fun createIncomingCallNotification(
        context: Context,
        channelId: String,
        callerName: String,
        phoneNumber: String,
        acceptCallIntent: PendingIntent,
        rejectCallIntent: PendingIntent
    ): Notification {

        val acceptAction = NotificationCompat.Action.Builder(
            R.drawable.ic_call_accept,
            context.getString(R.string.accept_call),
            acceptCallIntent
        ).build()

        val rejectAction = NotificationCompat.Action.Builder(
            R.drawable.ic_call_reject,
            context.getString(R.string.reject_call),
            rejectCallIntent
        ).build()

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle(callerName)
            .setContentText(phoneNumber)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(acceptAction)
            .addAction(rejectAction)
            .setTimeoutAfter(60000)
            .setFullScreenIntent(acceptCallIntent, true) // Pour affichage plein écran
            .build()
    }

    companion object {
        const val ACTION_START_CALL_SERVICE = "com.example.condo.ACTION_START_CALL_SERVICE"
        const val ACTION_ANSWER_CALL = "action_answer_call"
        const val ACTION_DECLINE_CALL = "action_decline_call"
        const val KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID = 5
    }
}