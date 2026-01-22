package com.idsolution.icondoapp.feature.rtsp

/**
 * Interface for native RTSP player handler
 * Implemented by platform-specific code (Swift on iOS, Kotlin on Android)
 *
 * This allows unified control of RTSP players from common code
 */
interface NativeRtspHandler {
    /**
     * Start playing the RTSP stream
     */
    fun play()

    /**
     * Pause the stream
     */
    fun pause()

    /**
     * Stop and release resources
     */
    fun stop()

    /**
     * Set audio volume (0-100)
     */
    fun setVolume(volume: Int)

    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean
}

/**
 * Callback interface for RTSP player events
 * Used to communicate state changes back to Kotlin
 */
interface RtspPlayerCallback {
    fun onStateChanged(state: RtspPlayerState)
    fun onError(message: String)
    fun onReady()
}
