package com.example.voip.voip.presenter

import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.linphone.mediastream.video.capture.CaptureTextureView

private const val TAG = "TextureViewScreen"

@Composable
fun TextureViewScreen(
    modifier: Modifier = Modifier,
    onTextureAvailable: (SurfaceTexture) -> Unit,
    onInitVideo: (TextureView, CaptureTextureView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var textureView by remember { mutableStateOf<TextureView?>(null) }
    var captureTextureView by remember { mutableStateOf<CaptureTextureView?>(null) }
    var videoInitialized by remember { mutableStateOf(false) }

    // Watch lifecycle to handle initialization correctly
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                textureView?.let { texture ->
                    captureTextureView?.let { capture ->
                        if (!videoInitialized) {
                            Log.d(TAG, "Initializing video on resume")
                            onInitVideo(texture, capture)
                            videoInitialized = true
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Remote video view
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            Log.d(TAG, "Remote surface texture available: $width x $height")
                            onTextureAvailable(surface)
                            textureView = this@apply

                            // Initialize video if both textures are ready
                            captureTextureView?.let { capture ->
                                if (!videoInitialized) {
                                    Log.d(TAG, "Initializing video on texture available")
                                    onInitVideo(this@apply, capture)
                                    videoInitialized = true
                                }
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            Log.d(TAG, "Remote surface size changed: $width x $height")
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            Log.d(TAG, "Remote surface destroyed")
                            textureView = null
                            videoInitialized = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                            // Texture is being drawn on - no action needed
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )

        // Local preview view
        AndroidView(
            factory = { ctx ->
                CaptureTextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // CaptureTextureView is a subclass of TextureView, so we can set a listener
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            Log.d(TAG, "Local preview surface available: $width x $height")
                            captureTextureView = this@apply

                            // Initialize video if both textures are ready
                            textureView?.let { texture ->
                                if (!videoInitialized) {
                                    Log.d(TAG, "Initializing video on preview available")
                                    onInitVideo(texture, this@apply)
                                    videoInitialized = true
                                }
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            Log.d(TAG, "Local surface size changed: $width x $height")
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            Log.d(TAG, "Local surface destroyed")
                            captureTextureView = null
                            videoInitialized = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                            // Texture is being drawn on - no action needed
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
        )
    }
}