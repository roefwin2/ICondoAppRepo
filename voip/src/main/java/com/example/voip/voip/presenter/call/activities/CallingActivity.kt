package com.example.voip.voip.presenter.call.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voip.voip.domain.ICondoVoip
import com.example.voip.voip.presenter.call.CallStateDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.linphone.core.Call

// CallingActivity.kt
class CallingActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setupCall()
        } else {
            Toast.makeText(this, "Permissions nécessaires pour l'appel", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val notificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vérifier les permissions au démarrage
        checkPermissions()
        // Récupérer les extras
        val answer = intent.getBooleanExtra("answer",true)
        notificationManagerCompat.cancel(2)
        setContent {
            MaterialTheme {
                val viewModel: VideoCallViewModel = koinViewModel()
                viewModel.answerCall()
                MainCallScreen(
                    viewModel = viewModel,
                    onCallEnded = { finish() }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            setupCall()
        }
    }

    private fun setupCall() {
        // Initialisation de l'appel ici
    }

    companion object {
        fun start(context: Context, callerId: String? = null) {
            val intent = Intent(context, CallingActivity::class.java).apply {
                callerId?.let { putExtra(EXTRA_CALLER_ID, it) }
            }
            context.startActivity(intent)
        }

        private const val EXTRA_CALLER_ID = "extra_caller_id"
    }
}

// VideoCallViewModel.kt
class VideoCallViewModel(
    private val iCondoVoip: ICondoVoip
) : ViewModel() {
    private val _callState = MutableStateFlow(CallState())
    val callState = _callState.asStateFlow()

    init {
        viewModelScope.launch {
            iCondoVoip.callState.collectLatest { state ->
                _callState.update {
                    it.copy(state = state.state, isCallActive = state.state != Call.State.Released)
                }
            }
        }
    }

    fun answerCall(){
        iCondoVoip.answerCall()
    }
    fun toggleMicrophone() {
        _callState.update { currentState ->
            currentState.copy(isMicEnabled = !currentState.isMicEnabled)
        }
    }

    fun toggleCamera() {
        iCondoVoip.toggleVideo()
        _callState.update { currentState ->
            currentState.copy(isCameraEnabled = !currentState.isCameraEnabled)
        }
    }

    fun endCall() {
        iCondoVoip.hangUp()
        _callState.update { currentState ->
            currentState.copy(isCallActive = false)
        }
    }
}

// CallState.kt
data class CallState(
    val state : Call.State = Call.State.Idle,
    val isCallActive: Boolean = true,
    val isMicEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true
)

// VideoCallScreen.kt
@Composable
fun MainCallScreen(
    viewModel: VideoCallViewModel = koinViewModel(),
    onCallEnded: () -> Unit
) {
    val callState by viewModel.callState.collectAsState()

    if (!callState.isCallActive) {
        LaunchedEffect(Unit) {
            onCallEnded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CallStateDisplay(callState = callState.state)
    }
}

@Composable
fun ControlBar(
    isMicEnabled: Boolean,
    isCameraEnabled: Boolean,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF1C1C1C))
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallControlButton(
            icon = if (isMicEnabled) Icons.Rounded.Mic else Icons.Rounded.MicOff,
            backgroundColor = if (isMicEnabled) Color.DarkGray else Color.Red,
            onClick = onToggleMic
        )

        CallControlButton(
            icon = if (isCameraEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
            backgroundColor = if (isCameraEnabled) Color.DarkGray else Color.Red,
            onClick = onToggleCamera
        )

        CallControlButton(
            icon = Icons.Rounded.CallEnd,
            backgroundColor = Color.Red,
            onClick = onEndCall
        )
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}