package com.idsolution.icondoapp.feature.rtsp

/**
 * Common RTSP player state for both platforms
 */
enum class RtspPlayerState {
    INIT,
    CONNECTING,
    BUFFERING,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR,
    ENDED
}

/**
 * RTSP player information
 */
data class RtspPlayerInfo(
    val state: RtspPlayerState = RtspPlayerState.INIT,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)
