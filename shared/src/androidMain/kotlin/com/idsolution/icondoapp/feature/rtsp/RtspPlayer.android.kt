package com.idsolution.icondoapp.feature.rtsp

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

private const val TAG = "VlcRtspPlayer"

@Composable
actual fun RtspPlayer(
    rtspUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var playerState by remember { mutableStateOf("INIT") }

    val (libVLC, mediaPlayer) = remember {
        Log.d(TAG, "=== Création LibVLC 3.5.4 ===")

        val vlc = LibVLC(context, arrayListOf(
            "-vvv",
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--rtsp-tcp"
        ))

        val player = MediaPlayer(vlc)

        vlc to player
    }

    // Event Listener pour LibVLC 3.x
    DisposableEffect(mediaPlayer) {
        val listener = object : MediaPlayer.EventListener {
            override fun onEvent(event: MediaPlayer.Event) {
                Log.d(TAG, "VLC Event: ${event.type}")

                when (event.type) {
                    MediaPlayer.Event.Opening -> {
                        playerState = "OPENING"
                        isLoading = true
                        Log.d(TAG, "▶ Opening...")
                    }
                    MediaPlayer.Event.Buffering -> {
                        val buffer = event.buffering
                        playerState = "BUFFERING ${buffer.toInt()}%"
                        isLoading = buffer < 100f
                        if (buffer >= 100f) {
                            Log.d(TAG, "✅ Buffering terminé")
                        }
                    }
                    MediaPlayer.Event.Playing -> {
                        playerState = "PLAYING"
                        isLoading = false
                        errorMessage = null
                        Log.d(TAG, "✅ PLAYING!")
                    }
                    MediaPlayer.Event.Paused -> {
                        playerState = "PAUSED"
                        Log.d(TAG, "⏸ Paused")
                    }
                    MediaPlayer.Event.Stopped -> {
                        playerState = "STOPPED"
                        isLoading = false
                        Log.d(TAG, "⏹ Stopped")
                    }
                    MediaPlayer.Event.EncounteredError -> {
                        playerState = "ERROR"
                        isLoading = false
                        errorMessage = "Erreur de lecture\nVérifiez l'URL et la connexion"
                        Log.e(TAG, "❌ ERROR")
                    }
                    MediaPlayer.Event.EndReached -> {
                        playerState = "ENDED"
                        Log.d(TAG, "Stream terminé")
                    }
                    MediaPlayer.Event.Vout -> {
                        Log.d(TAG, "🎥 VIDEO OUTPUT! Vout count: ${event.voutCount}")
                    }
                }
            }
        }

        mediaPlayer.setEventListener(listener)

        onDispose {
            mediaPlayer.setEventListener(null)
        }
    }

    // Démarrer lecture
    LaunchedEffect(rtspUrl) {
        Log.d(TAG, "Configuration média: $rtspUrl")

        try {
            val media = Media(libVLC, Uri.parse(rtspUrl)).apply {
                setHWDecoderEnabled(true, false)
                addOption(":network-caching=300")
                addOption(":rtsp-tcp")
            }

            mediaPlayer.media = media
            media.release()

            Log.d(TAG, "Démarrage lecture...")
            mediaPlayer.play()

        } catch (e: Exception) {
            Log.e(TAG, "Erreur configuration", e)
            errorMessage = "Erreur: ${e.message}"
        }
    }

//    // Controller
//    LaunchedEffect(mediaPlayer) {
//        onControllerReady?.invoke(VlcPlayerController(mediaPlayer))
//    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Cleanup")
            mediaPlayer.stop()
            mediaPlayer.release()
            libVLC.release()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // Video Layout
        AndroidView(
            factory = { ctx ->
                Log.d(TAG, "Création VLCVideoLayout")
                VLCVideoLayout(ctx).apply {
                    mediaPlayer.attachViews(this, null, false, false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // État
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            color = Color.Black.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = playerState,
                color = when {
                    playerState == "PLAYING" -> Color.Green
                    playerState.startsWith("BUFFERING") -> Color.Yellow
                    playerState == "ERROR" -> Color.Red
                    else -> Color.White
                },
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Loading
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connexion...", color = Color.White)
                    Text(playerState, color = Color.Gray)
                }
            }
        }

        // Error
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Card(modifier = Modifier.padding(24.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("❌ Erreur", color = Color.Red, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error, color = Color.Black)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            errorMessage = null
                            isLoading = true
                            mediaPlayer.stop()
                            mediaPlayer.play()
                        }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
        }
    }
}
