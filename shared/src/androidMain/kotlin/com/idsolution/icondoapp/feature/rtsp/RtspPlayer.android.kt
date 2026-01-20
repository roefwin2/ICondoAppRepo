package com.idsolution.icondoapp.feature.rtsp

import android.util.Log
import android.view.ViewGroup
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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView

private const val TAG = "ExoRtspPlayer"

@Composable
actual fun RtspPlayer(
    rtspUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var playerState by remember { mutableStateOf("INIT") }

    val exoPlayer = remember {
        Log.d(TAG, "=== Creating ExoPlayer for RTSP ===")
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            volume = 0f // Mute by default for RTSP streams (usually security cameras)
        }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        playerState = "IDLE"
                        Log.d(TAG, "State: IDLE")
                    }
                    Player.STATE_BUFFERING -> {
                        playerState = "BUFFERING"
                        isLoading = true
                        Log.d(TAG, "State: BUFFERING")
                    }
                    Player.STATE_READY -> {
                        playerState = "PLAYING"
                        isLoading = false
                        errorMessage = null
                        Log.d(TAG, "State: READY/PLAYING")
                    }
                    Player.STATE_ENDED -> {
                        playerState = "ENDED"
                        isLoading = false
                        Log.d(TAG, "State: ENDED")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    playerState = "PLAYING"
                    isLoading = false
                }
                Log.d(TAG, "isPlaying: $isPlaying")
            }

            override fun onPlayerError(error: PlaybackException) {
                playerState = "ERROR"
                isLoading = false
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "Connection failed\nCheck network and URL"
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "Connection timeout\nCheck network connectivity"
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                        "Unsupported format"
                    else ->
                        "Playback error\nCode: ${error.errorCode}"
                }
                Log.e(TAG, "Player error: ${error.message}", error)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Start playback
    LaunchedEffect(rtspUrl) {
        Log.d(TAG, "Setting up RTSP media: $rtspUrl")

        try {
            // Create RTSP media source with TCP transport (more reliable)
            val mediaItem = MediaItem.Builder()
                .setUri(rtspUrl)
                .build()

            val rtspMediaSource = RtspMediaSource.Factory()
                .setForceUseRtpTcp(true) // Use TCP for better reliability
                .setDebugLoggingEnabled(true)
                .createMediaSource(mediaItem)

            exoPlayer.setMediaSource(rtspMediaSource)
            exoPlayer.prepare()

            Log.d(TAG, "Playback prepared, starting...")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up media", e)
            errorMessage = "Error: ${e.message}"
            isLoading = false
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Cleanup - releasing ExoPlayer")
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // Video View
        AndroidView(
            factory = { ctx ->
                Log.d(TAG, "Creating PlayerView")
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Hide default controls for security camera view
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )

        // Status indicator
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
                    playerState == "BUFFERING" -> Color.Yellow
                    playerState == "ERROR" -> Color.Red
                    else -> Color.White
                },
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Loading indicator
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
                    Text("Connecting...", color = Color.White)
                    Text(playerState, color = Color.Gray)
                }
            }
        }

        // Error display
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
                        Text(
                            "Error",
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error, color = Color.Black)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            errorMessage = null
                            isLoading = true
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
