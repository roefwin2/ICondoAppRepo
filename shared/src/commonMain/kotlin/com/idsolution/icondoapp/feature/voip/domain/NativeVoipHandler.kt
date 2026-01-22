package com.idsolution.icondoapp.feature.voip.domain

/**
 * Interface for native VoIP handler
 * Implemented by platform-specific code (Swift/LinphoneManager on iOS, AndroidVoipService on Android)
 *
 * This interface defines the contract between Kotlin common code and native implementations
 */
interface NativeVoipHandler {
    /**
     * Initialize the VoIP stack
     */
    fun initialize()

    /**
     * Login to SIP server
     * @param username SIP username
     * @param password SIP password
     * @param domain SIP domain
     * @param transport Transport type (udp, tcp, tls)
     */
    fun login(username: String, password: String, domain: String, transport: String)

    /**
     * Logout from SIP server
     */
    fun logout()

    /**
     * Make a call to a SIP URI
     * @param sipUri The SIP URI to call (e.g., "sip:user@domain.com")
     */
    fun makeCall(sipUri: String)

    /**
     * Answer an incoming call
     */
    fun answerCall()

    /**
     * Hang up the current call
     */
    fun hangUp()

    /**
     * Toggle video on/off
     */
    fun toggleVideo()

    /**
     * Switch between front and back camera
     */
    fun toggleCamera()

    /**
     * Pause or resume the current call
     */
    fun pauseOrResume()

    /**
     * Toggle microphone mute
     */
    fun toggleMute()

    /**
     * Toggle speaker output
     */
    fun toggleSpeaker()

    /**
     * Clean up and release resources
     */
    fun destroy()
}

/**
 * Callback interface for VoIP events
 * Used to communicate state changes from native code back to Kotlin
 */
interface VoipStateCallback {
    /**
     * Called when registration state changes
     */
    fun onRegistrationStateChanged(
        state: String,
        message: String,
        isRegistered: Boolean
    )

    /**
     * Called when call state changes
     */
    fun onCallStateChanged(
        state: String,
        remoteAddress: String?,
        remoteName: String?,
        isVideoEnabled: Boolean,
        duration: Long
    )
}
