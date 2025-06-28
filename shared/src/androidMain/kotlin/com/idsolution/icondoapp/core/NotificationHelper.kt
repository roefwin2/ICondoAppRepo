package com.idsolution.icondoapp.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.idsolution.icondoapp.CallActivity
import com.idsolution.icondoapp.MainActivity
import com.idsolution.icondoapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

class NotificationHelper(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    private val serverBaseUrl = "http://192.168.1.18:3000" // Changez selon votre serveur

    // Enregistrer un utilisateur avec son token de notification
    fun registerUserWithNotification(
        phoneNumber: String,
        userId: String,
        notificationToken: String?,
        platform: String = "android",
        callback: (Boolean, String?) -> Unit
    ) {
        println("📱 [NotificationHelper] Enregistrement avec notification: $phoneNumber")

        scope.launch {
            try {
                val success = registerWithServer(phoneNumber, userId, notificationToken, platform)

                if (success) {
                    println("📱 [NotificationHelper] ✅ Enregistrement réussi")
                    callback(true, null)
                } else {
                    println("📱 [NotificationHelper] ❌ Échec enregistrement")
                    callback(false, "Erreur lors de l'enregistrement")
                }

            } catch (e: Exception) {
                println("📱 [NotificationHelper] ❌ Exception: ${e.message}")
                callback(false, e.message)
            }
        }
    }

    // Enregistrer juste le token de notification pour un utilisateur existant
    fun registerNotificationToken(
        userId: String,
        notificationToken: String,
        platform: String = "android",
        callback: (Boolean, String?) -> Unit
    ) {
        println("📱 [NotificationHelper] Enregistrement token: ${notificationToken.take(20)}...")

        scope.launch {
            try {
                val success = registerTokenWithServer(userId, notificationToken, platform)

                if (success) {
                    println("📱 [NotificationHelper] ✅ Token enregistré")
                    callback(true, null)
                } else {
                    println("📱 [NotificationHelper] ❌ Échec enregistrement token")
                    callback(false, "Erreur lors de l'enregistrement du token")
                }

            } catch (e: Exception) {
                println("📱 [NotificationHelper] ❌ Exception token: ${e.message}")
                callback(false, e.message)
            }
        }
    }

    private suspend fun registerWithServer(
        phoneNumber: String,
        userId: String,
        notificationToken: String?,
        platform: String
    ): Boolean {
        return try {
            val url = URL("$serverBaseUrl/api/users/register")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val deviceInfo = mapOf(
                "model" to android.os.Build.MODEL,
                "brand" to android.os.Build.BRAND,
                "version" to android.os.Build.VERSION.RELEASE,
                "sdk" to android.os.Build.VERSION.SDK_INT.toString()
            )

            val requestBody = buildString {
                append("{")
                append("\"phoneNumber\": \"$phoneNumber\",")
                append("\"userID\": \"$userId\",")
                append("\"platform\": \"$platform\",")
                if (notificationToken != null) {
                    append("\"notificationToken\": \"$notificationToken\",")
                }
                append("\"deviceInfo\": ${json.encodeToString(kotlinx.serialization.serializer(), deviceInfo)}")
                append("}")
            }

            println("📱 [NotificationHelper] Request body: $requestBody")

            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            val responseCode = connection.responseCode
            println("📱 [NotificationHelper] Response code: $responseCode")

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                println("📱 [NotificationHelper] Response: $response")
            }

            responseCode == 200
        } catch (e: Exception) {
            println("📱 [NotificationHelper] ❌ Erreur API register: ${e.message}")
            false
        }
    }

    private suspend fun registerTokenWithServer(
        userId: String,
        notificationToken: String,
        platform: String
    ): Boolean {
        return try {
            val url = URL("$serverBaseUrl/api/notifications/register-token")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = """
                {
                    "userID": "$userId",
                    "token": "$notificationToken",
                    "platform": "$platform"
                }
            """.trimIndent()

            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            val responseCode = connection.responseCode
            println("📱 [NotificationHelper] Token register response: $responseCode")

            responseCode == 200
        } catch (e: Exception) {
            println("📱 [NotificationHelper] ❌ Erreur API token: ${e.message}")
            false
        }
    }
}

// Service pour gérer les notifications Firebase
class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("📱 [FCM] Nouveau token reçu: ${token.take(20)}...")

        // Sauvegarder le token localement
        val prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // Envoyer au serveur si on a un userId
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getString("user_id", null)

        if (userId != null) {
            val notificationHelper = NotificationHelper(this)
            notificationHelper.registerNotificationToken(userId, token) { success, error ->
                if (success) {
                    println("📱 [FCM] Token mis à jour sur le serveur")
                } else {
                    println("📱 [FCM] Erreur mise à jour token: $error")
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("📱 [FCM] Message reçu de: ${remoteMessage.from}")

        // Vérifier si c'est un appel entrant
        val data = remoteMessage.data
        val notificationType = data["notificationType"]

        if (notificationType == "incoming_call") {
            handleIncomingCallNotification(data)
        } else {
            // Notification standard
            remoteMessage.notification?.let {
                println("📱 [FCM] Notification: ${it.title} - ${it.body}")
                showNotification(it.title ?: "Notification", it.body ?: "")
            }
        }
    }

    private fun handleIncomingCallNotification(data: Map<String, String>) {
        println("📞 [FCM] Notification d'appel entrant reçue")

        val callerName = data["callerName"] ?: "Inconnu"
        val callerPhoneNumber = data["callerPhoneNumber"]
        val roomID = data["roomID"]
        val isPhoneCall = data["isPhoneCall"]?.toBoolean() ?: false

        // Créer une notification d'appel entrant
        val title = if (isPhoneCall) "📞 Appel entrant" else "Appel entrant"
        val body = if (callerPhoneNumber != null) {
            "$callerName ($callerPhoneNumber) vous appelle"
        } else {
            "$callerName vous appelle"
        }

        // Créer des actions pour répondre/rejeter
        showIncomingCallNotification(title, body, roomID, data)
    }

    private fun showIncomingCallNotification(
        title: String,
        body: String,
        roomID: String?,
        callData: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal de notification pour les appels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "incoming_calls",
                "Appels entrants",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les appels entrants"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir l'app et répondre à l'appel
        val answerIntent = Intent(this, CallActivity::class.java).apply {
            action = "ANSWER_CALL"
            putExtra("roomID", roomID)
            putExtra("callerName", callData["callerName"])
            putExtra("callerPhoneNumber", callData["callerPhoneNumber"])
            putExtra("isPhoneCall", callData["isPhoneCall"]?.toBoolean() ?: false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val answerPendingIntent = PendingIntent.getActivity(
            this, 0, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent pour rejeter l'appel
        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "REJECT_CALL"
            putExtra("roomID", roomID)
        }

        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 1, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construire la notification
        val notification = NotificationCompat.Builder(this, "incoming_calls")
            .setSmallIcon(com.example.voip.R.drawable.ic_call_notification) // Vous devez ajouter cette icône
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(answerPendingIntent, true)
            .addAction(
                com.example.voip.R.drawable.ic_call_reject, // Icône rejeter
                "Rejeter",
                rejectPendingIntent
            )
            .addAction(
                com.example.voip.R.drawable.ic_call_accept, // Icône répondre
                "Répondre",
                answerPendingIntent
            )
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            .build()

        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, notification)

        println("📞 [FCM] Notification d'appel affichée")
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal pour les notifications générales
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "general",
                "Notifications générales",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "general")
            .setSmallIcon(com.example.voip.R.drawable.ic_call_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val INCOMING_CALL_NOTIFICATION_ID = 1001
    }
}

// BroadcastReceiver pour gérer les actions de notification
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val roomID = intent.getStringExtra("roomID")

        println("📞 [CallAction] Action reçue: $action pour room: $roomID")

        when (action) {
            "REJECT_CALL" -> {
                // Rejeter l'appel
                handleRejectCall(context, roomID)

                // Supprimer la notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1001) // INCOMING_CALL_NOTIFICATION_ID
            }
        }
    }

    private fun handleRejectCall(context: Context, roomID: String?) {
        println("📞 [CallAction] Rejet de l'appel pour room: $roomID")

        // Ici vous pouvez envoyer un signal au serveur pour indiquer que l'appel est rejeté
        // Ou utiliser un EventBus pour notifier l'app

        // Exemple avec SharedPreferences pour communiquer avec l'app
        val prefs = context.getSharedPreferences("call_actions", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("last_action", "REJECT_CALL")
            .putString("last_room_id", roomID)
            .putLong("action_timestamp", System.currentTimeMillis())
            .apply()
    }
}

// Extension pour obtenir le token FCM facilement
fun Context.getFCMToken(callback: (String?) -> Unit) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            println("📱 [FCM] Erreur récupération token: ${task.exception}")
            callback(null)
            return@addOnCompleteListener
        }

        val token = task.result
        println("📱 [FCM] Token récupéré: ${token?.take(20)}...")
        callback(token)
    }
}