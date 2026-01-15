package com.idsolution.icondoapp.feature.voip

import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService

/**
 * Factory for creating platform-specific VoIP service instances
 *
 * Usage:
 * ```
 * val voipService = VoipServiceFactory.create(eventHandler)
 * voipService.initialize()
 * voipService.login(username, password, domain)
 * ```
 */
expect object VoipServiceFactory {
    /**
     * Create a new VoIP service instance
     * @param eventHandler Handler for VoIP events (optional)
     * @return Platform-specific VoIP service implementation
     */
    fun create(eventHandler: VoipEventHandler? = null): VoipService
}
