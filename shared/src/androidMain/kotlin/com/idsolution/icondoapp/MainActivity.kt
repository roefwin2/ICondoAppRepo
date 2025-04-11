package com.idsolution.icondoapp

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.testkmpapp.feature.mainscreen.NavigationRoot
import com.example.testkmpapp.theme.CondoTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.linphone.core.tools.service.CoreService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We will need the RECORD_AUDIO permission for video call
        if (packageManager.checkPermission(
                Manifest.permission.RECORD_AUDIO,
                packageName
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
        requestPermissions()
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

    // Exemple de code pour demander les permissions à l'exécution
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )
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
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    GreetingView("Hello, Android!")
}
