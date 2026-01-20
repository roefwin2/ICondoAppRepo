package com.idsolution.icondoapp.feature.voip.domain

/**
 * Handler for VoIP events that need to communicate with other parts of the app
 * For example, when a call comes in from a door intercom, this can trigger door opening
 */
interface VoipEventHandler {
    /**
     * Called when a door open is requested from VoIP (e.g., DTMF tone during call)
     */
    fun onDoorOpenRequested()

    /**
     * Called when an incoming call is received
     * @param callerUri The SIP URI of the caller
     * @param callerName The display name of the caller (if available)
     */
    fun onIncomingCall(callerUri: String, callerName: String?)

    /**
     * Called when a call is connected
     */
    fun onCallConnected()

    /**
     * Called when a call ends
     */
    fun onCallEnded()
}

/**
 * Default no-op implementation
 */
class DefaultVoipEventHandler : VoipEventHandler {
    override fun onDoorOpenRequested() {
        println("VoipEventHandler: Door open requested (default handler)")
    }

    override fun onIncomingCall(callerUri: String, callerName: String?) {
        println("VoipEventHandler: Incoming call from $callerUri")
    }

    override fun onCallConnected() {
        println("VoipEventHandler: Call connected")
    }

    override fun onCallEnded() {
        println("VoipEventHandler: Call ended")
    }
}
