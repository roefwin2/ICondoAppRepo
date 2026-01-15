package com.idsolution.icondoapp.feature.voip

import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.service.IOSVoipService
import com.idsolution.icondoapp.feature.voip.service.NativeVoipHandler

/**
 * iOS implementation of VoipServiceFactory
 */
actual object VoipServiceFactory {
    private var instance: IOSVoipService? = null

    /**
     * Create iOS VoIP service
     */
    actual fun create(eventHandler: VoipEventHandler?): VoipService {
        return instance ?: IOSVoipService(eventHandler).also {
            instance = it
        }
    }

    /**
     * Get the current instance
     */
    fun getInstance(): IOSVoipService? = instance

    /**
     * Set the native iOS handler from Swift
     */
    fun setNativeHandler(handler: NativeVoipHandler) {
        instance?.setNativeHandler(handler)
    }

    /**
     * Clear the instance
     */
    fun clear() {
        instance?.destroy()
        instance = null
    }
}
