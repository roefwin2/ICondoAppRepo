package com.idsolution.icondoapp.feature.voip

import com.example.voip.voip.domain.VoipEventHandler
import com.idsolution.icondoapp.core.domain.briges.VoipDoorBridge

class VoipEventHandlerImpl : VoipEventHandler {
    override fun onDoorOpenRequested() {
        println("[VoipEventHandlerImpl] Door open requested from Android call")
        VoipDoorBridge.requestDoorOpenSync()
    }
}