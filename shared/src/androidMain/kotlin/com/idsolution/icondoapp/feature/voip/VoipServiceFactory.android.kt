package com.idsolution.icondoapp.feature.voip

import android.content.Context
import com.idsolution.icondoapp.feature.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.feature.voip.domain.VoipService
import com.idsolution.icondoapp.feature.voip.service.AndroidVoipService

/**
 * Android implementation of VoipServiceFactory
 */
actual object VoipServiceFactory {
    private var context: Context? = null
    private var instance: AndroidVoipService? = null

    /**
     * Initialize the factory with Android context
     * Must be called before creating VoIP service
     */
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    /**
     * Create Android VoIP service using Linphone
     */
    actual fun create(eventHandler: VoipEventHandler?): VoipService {
        val ctx = context ?: throw IllegalStateException(
            "VoipServiceFactory not initialized. Call VoipServiceFactory.init(context) first."
        )

        // Return existing instance if available (singleton pattern)
        return instance ?: AndroidVoipService(ctx, eventHandler).also {
            instance = it
        }
    }

    /**
     * Get the current instance (if exists)
     */
    fun getInstance(): AndroidVoipService? = instance

    /**
     * Clear the instance (for cleanup)
     */
    fun clear() {
        instance?.destroy()
        instance = null
    }
}
