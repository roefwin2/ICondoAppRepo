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
    private val notificationId = 1000

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
                    hideIncomingCallNotification()
                    showInCallNotification(call)
                }

                Call.State.Released, Call.State.End, Call.State.Error -> {
                    hideAllNotifications()
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

        // Ajout du listener au core Linphone
        try {
            val core = (iCondoVoip as ICondoLinphoneImpl).getCore()
            core.addListener(coreListener)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout du listener: $e")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CALL_SERVICE -> {
                startKeepAliveForeground()
            }

            ACTION_ANSWER_CALL -> {
                handleAnswerCall()
            }

            ACTION_DECLINE_CALL -> {
                handleDeclineCall()
            }
        }
        return START_STICKY
    }

    private fun showIncomingCallNotification(call: Call) {
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
            this, callerName, phoneNumber, "test", answerPendingIntent, declinePendingIntent
        )

        startForeground(notificationId, notification)

        // Optionnel: lancer l'activité d'appel entrant
        val callActivityIntent = Intent(this, CallingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("incoming_call", true)
            putExtra("caller_name", callerName)
            putExtra("caller_number", phoneNumber)
        }
        iCondoVoip.answerCall()
        startActivity(callActivityIntent)
    }

    private fun handleAnswerCall() {
        Log.i(TAG, "Réponse à l'appel")
        iCondoVoip.answerCall()
        hideIncomingCallNotification()
    }

    private fun handleDeclineCall() {
        Log.i(TAG, "Refus de l'appel")
        iCondoVoip.hangUp()
        hideIncomingCallNotification()
    }

    private fun hideIncomingCallNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun hideAllNotifications() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun showInCallNotification(call: Call) {
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
            .build()

        startForeground(notificationId + 1, notification)
    }

    private fun startKeepAliveForeground() {
        val channelId = getString(R.string.notification_channel_service_id)
        createNotificationChannel(channelId, "Service VoIP", NotificationManager.IMPORTANCE_LOW)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Service VoIP")
            .setContentText("En attente d'appels...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID, notification)
    }

    private fun createNotificationChannel(channelId: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "CallService destroyed")
        try {
            val core = (iCondoVoip as ICondoLinphoneImpl).core.removeListener(coreListener)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression du listener: $e")
        }
        super.onDestroy()
    }

    override fun createServiceNotificationChannel() {
        createNotificationChannel(channelId, "Appels entrants", NotificationManager.IMPORTANCE_HIGH)
    }

    override fun showForegroundServiceNotification(isVideoCall: Boolean) {
        // Cette méthode sera appelée automatiquement par CoreService
        // On laisse notre gestion dans onCallStateChanged
    }

    override fun hideForegroundServiceNotification() {
        hideAllNotifications()
    }

    private fun createIncomingCallNotification(
        context: Context,
        channelId: String,
        callerName: String,
        phoneNumber: String,
        acceptCallIntent: PendingIntent,
        rejectCallIntent: PendingIntent
    ): Notification {

        // Création des actions pour répondre/rejeter l'appel
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

        // Configuration de la notification d'appel entrant
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle(callerName)
            .setContentText(phoneNumber)
            //.setLargeIcon(getCallerAvatar(context, phoneNumber)) // Méthode à implémenter pour récupérer l'avatar
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorité élevée
            .setAutoCancel(true)
            .setOngoing(false)
            .addAction(acceptAction)
            .addAction(rejectAction)
            .setTimeoutAfter(60000) // Timeout après 1 minute
            .build()
    }

    @MainThread
    private fun stopKeepAliveServiceForeground() {
        Log.i(
            "$TAG Stopping keep alive for third party accounts foreground Service (was using notification ID)"
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ACTION_START_CALL_SERVICE = "com.example.condo.ACTION_START_CALL_SERVICE"
        const val ACTION_ANSWER_CALL = "action_answer_call"
        const val ACTION_DECLINE_CALL = "action_decline_call"
        const val KEEP_ALIVE_FOR_THIRD_PARTY_ACCOUNTS_ID = 5
    }
}
