package com.idsolution.icondoapp.feature.voip.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for VoIP service - Multiplatform abstraction
 *
 * This interface defines all VoIP operations that can be implemented
 * differently on each platform (Android uses Linphone, iOS uses Linphone/CallKit)
 */
interface VoipService {

    /**
     * Current call state as a Flow
     */
    val callState: StateFlow<VoipCallInfo>

    /**
     * Current account/registration state as a Flow
     */
    val accountState: StateFlow<VoipAccountState>

    /**
     * Initialize the VoIP service
     * Must be called before any other operation
     */
    fun initialize()

    /**
     * Login to SIP server
     * @param username SIP username
     * @param password SIP password
     * @param domain SIP server domain
     * @param transportType Transport protocol (UDP, TCP, TLS)
     */
    fun login(
        username: String,
        password: String,
        domain: String,
        transportType: VoipTransportType = VoipTransportType.UDP
    )

    /**
     * Logout from SIP server
     */
    fun logout()

    /**
     * Make an outgoing call
     * @param sipUri Full SIP URI to call (e.g., sip:user@domain.com)
     */
    fun makeCall(sipUri: String)

    /**
     * Answer an incoming call
     */
    fun answerCall()

    /**
     * Hang up the current call or decline an incoming call
     */
    fun hangUp()

    /**
     * Toggle video on/off during a call
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
     * Mute or unmute microphone
     */
    fun toggleMute()

    /**
     * Toggle speaker on/off
     */
    fun toggleSpeaker()

    /**
     * Check if service is initialized
     */
    fun isInitialized(): Boolean

    /**
     * Check if user is registered
     */
    fun isRegistered(): Boolean

    /**
     * Start background service for incoming calls (platform specific)
     */
    fun startBackgroundService()

    /**
     * Stop background service
     */
    fun stopBackgroundService()

    /**
     * Clean up resources
     */
    fun destroy()
}
