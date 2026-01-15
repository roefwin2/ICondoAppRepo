package com.idsolution.icondoapp.feature.voip.domain

/**
 * Represents the state of a VoIP call
 */
enum class CallState {
    IDLE,
    OUTGOING_INIT,
    OUTGOING_PROGRESS,
    OUTGOING_RINGING,
    OUTGOING_EARLY_MEDIA,
    INCOMING_RECEIVED,
    INCOMING_EARLY_MEDIA,
    CONNECTED,
    STREAMS_RUNNING,
    PAUSING,
    PAUSED,
    RESUMING,
    UPDATING,
    UPDATED_BY_REMOTE,
    ERROR,
    END,
    RELEASED
}

/**
 * Represents VoIP account registration state
 */
enum class RegistrationState {
    NONE,
    PROGRESS,
    OK,
    CLEARED,
    FAILED
}

/**
 * VoIP call information
 */
data class VoipCallInfo(
    val state: CallState = CallState.IDLE,
    val remoteAddress: String? = null,
    val remoteName: String? = null,
    val isVideoEnabled: Boolean = false,
    val isAudioMuted: Boolean = false,
    val isCameraFront: Boolean = true,
    val duration: Long = 0
)

/**
 * VoIP account state
 */
data class VoipAccountState(
    val registrationState: RegistrationState = RegistrationState.NONE,
    val message: String = "",
    val isRegistered: Boolean = false
)

/**
 * Transport type for SIP
 */
enum class VoipTransportType {
    UDP,
    TCP,
    TLS
}
