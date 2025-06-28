package com.idsolution.icondoapp

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bouyahya.kmpcall.ui.CallScreen
import com.example.testkmpapp.feature.mainscreen.NavigationRoot
import com.example.testkmpapp.theme.CondoTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.idsolution.icondoapp.core.NotificationHelper
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.linphone.core.tools.service.CoreService
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var notificationHelper: NotificationHelper

    // Informations utilisateur (à remplacer par votre logique)
    private var userPhoneNumber: String = "+33651690406"
    private var userId: String = "david@ldctechnologie.com"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions(this, this)
        notificationHelper = NotificationHelper(this)

        // Vérifier si on arrive via une notification d'appel
        handleIncomingCallIntent(intent)
        registerFCMToken()
        enableEdgeToEdge()
        setContent {
            CondoTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavigationRoot(
                        onIncomingCall = {
                            showNotification(it)
                        },
                        onErrorLogin = {
                            scope.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let { handleIncomingCallIntent(it) }
    }

    private fun handleIncomingCallIntent(intent: Intent) {
        when (intent.action) {
            "ANSWER_CALL" -> {
                val roomID = intent.getStringExtra("roomID")
                val callerName = intent.getStringExtra("callerName")
                val callerPhoneNumber = intent.getStringExtra("callerPhoneNumber")
                val isPhoneCall = intent.getBooleanExtra("isPhoneCall", false)

                println("📞 [MainActivity] Réponse appel: room=$roomID, caller=$callerName")

                // Ouvrir directement l'écran d'appel avec les bonnes informations
                if (roomID != null) {
                    openCallScreen(roomID, callerName, isPhoneCall)
                }
            }
        }
    }

    private fun showNotification(title: String) {
        val notification = NotificationCompat.Builder(applicationContext, "condo_channel_id")
            .setContentTitle(title)
            .setContentText("This is a description")
            .build()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun checkAndRequestPermissions(context: Context, activity: Activity) {
        val permissionsToRequest = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionsNeeded = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest,
                0
            )
        }
    }

    private fun openCallScreen(roomID: String, callerName: String?, isPhoneCall: Boolean) {
        // Cette méthode sera appelée quand l'utilisateur répond à la notification
        println("📞 [MainActivity] Ouverture écran d'appel: $roomID")

        // Vous pouvez utiliser votre système de navigation ici
        // Par exemple avec Navigation Compose ou en créant une nouvelle Activity
    }

    @Composable
    fun CallApp() {
        var isRegistered by remember { mutableStateOf(false) }
        var registrationError by remember { mutableStateOf<String?>(null) }

        // Vérifier si l'utilisateur est déjà enregistré
        LaunchedEffect(Unit) {
            /*checkUserRegistration { registered, phone, id ->
                isRegistered = registered
                if (registered) {
                    userPhoneNumber = phone ?: ""
                    userId = id ?: ""

                    // Enregistrer le token FCM
                    registerFCMToken()
                }
            }*/
        }

        if (isRegistered) {
            CallScreen(
                onDisconnect = { finish() },
                //userPhoneNumber = userPhoneNumber,
                userId = userId
            )
        } else {
            RegistrationScreen(
                onRegistrationComplete = { phone, id ->
                    userPhoneNumber = phone
                    userId = id
                    isRegistered = true

                    // Enregistrer le token FCM après inscription
                    registerFCMToken()
                },
                onError = { error ->
                    registrationError = error
                }
            )
        }
    }

    @Composable
    fun RegistrationScreen(
        onRegistrationComplete: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        var phoneNumber by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📱 Enregistrement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Numéro de téléphone") },
                placeholder = { Text("+33612345678") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        isLoading = true
                        registerUser(phoneNumber.trim()) { success, error, phone, id ->
                            isLoading = false
                            if (success && phone != null && id != null) {
                                onRegistrationComplete(phone, id)
                            } else {
                                onError(error ?: "Erreur d'enregistrement")
                            }
                        }
                    }
                },
                enabled = !isLoading && phoneNumber.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("S'enregistrer")
                }
            }
        }
    }

    private fun registerUser(
        phoneNumber: String,
        callback: (Boolean, String?, String?, String?) -> Unit
    ) {
        // Générer un userId unique
        val userId = "user_${UUID.randomUUID().toString().take(8)}"

        println("📱 [MainActivity] Enregistrement: $phoneNumber -> $userId")

        // D'abord obtenir le token FCM
        getFCMToken { fcmToken ->
            // Enregistrer avec le serveur
            notificationHelper.registerUserWithNotification(
                phoneNumber = phoneNumber,
                userId = userId,
                notificationToken = fcmToken,
                platform = "android"
            ) { success, error ->
                if (success) {
                    // Sauvegarder localement
                    saveUserToPrefs(phoneNumber, userId)
                    println("📱 [MainActivity] ✅ Enregistrement réussi")
                    callback(true, null, phoneNumber, userId)
                } else {
                    println("📱 [MainActivity] ❌ Erreur: $error")
                    callback(false, error, null, null)
                }
            }
        }
    }

    private fun saveUserToPrefs(phoneNumber: String, userId: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("phone_number", phoneNumber)
            .putString("user_id", userId)
            .putLong("registered_at", System.currentTimeMillis())
            .apply()
    }

    private fun registerFCMToken() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        getFCMToken { token ->
            if (token != null) {
                notificationHelper.registerNotificationToken(
                    userId = userId,
                    notificationToken = token,
                    platform = "android"
                ) { success, error ->
                    if (success) {
                        println("📱 [MainActivity] ✅ Token FCM enregistré")
                    } else {
                        println("📱 [MainActivity] ❌ Erreur token FCM: $error")
                    }
                }
            }
        }
    }
}

// Activity spécialisée pour les appels
class CallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupérer les informations de l'appel depuis l'intent
        val roomID = intent.getStringExtra("roomID")
        val callerName = intent.getStringExtra("callerName")
        val callerPhoneNumber = intent.getStringExtra("callerPhoneNumber")
        val isPhoneCall = intent.getBooleanExtra("isPhoneCall", false)

        println("📞 [CallActivity] Démarrage avec room: $roomID")

        // Récupérer les informations utilisateur
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userPhoneNumber = prefs.getString("phone_number", "") ?: ""
        val userId = prefs.getString("user_id", "") ?: ""

        setContent {
            CallScreen(
                onDisconnect = {
                    finish()
                },
                //userPhoneNumber = userPhoneNumber,
                userId = userId
            )
        }

        // Supprimer la notification d'appel entrant si elle existe
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001) // INCOMING_CALL_NOTIFICATION_ID
    }
}

// Extension pour faciliter l'accès au token FCM
private fun Context.getFCMToken(callback: (String?) -> Unit) {
    try {
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
    } catch (e: Exception) {
        println("📱 [FCM] Exception token: ${e.message}")
        callback(null)
    }
}

