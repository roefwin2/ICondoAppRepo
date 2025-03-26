package com.example.voip.voip.presenter

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.linphone.mediastream.video.capture.CaptureTextureView

@Composable
fun TextureViewScreen(
    modifier: Modifier = Modifier,
    onTextureAvailable: (SurfaceTexture) -> Unit,
    onInitVideo: ((TextureView, CaptureTextureView) -> Unit)
) {
    val context = LocalContext.current

    var textureView by remember { mutableStateOf(TextureView(context)) }
    var captureTextureView by remember { mutableStateOf(CaptureTextureView(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // TextureView
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    textureView = this
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            onTextureAvailable(surface)
                            onInitVideo.invoke(textureView, captureTextureView)
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            // Handle size change
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            // Handle destruction
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                            // Handle updates
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Set appropriate height
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CaptureTextureView (Custom View Example)
        AndroidView(
            factory = { ctx ->
                CaptureTextureView(ctx).apply {
                    // Set up properties for your CaptureTextureView if needed
                    captureTextureView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Set appropriate height
        )
    }
}