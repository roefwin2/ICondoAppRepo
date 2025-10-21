package com.idsolution.icondoapp.core.domain.briges

import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object VoipDoorBridge {

    object DoorOpenRequest

    private val _doorOpenEvents = MutableSharedFlow<DoorOpenRequest>(replay = 0)
    val doorOpenEvents = _doorOpenEvents.asSharedFlow()

    suspend fun requestDoorOpen() {
        println("[VoipDoorBridge] Requesting door open: site=$}, door=$")
        _doorOpenEvents.emit(DoorOpenRequest)
    }

    // Version synchrone pour Swift/Kotlin/JVM (lance une coroutine)
    fun requestDoorOpenSync() {
        kotlinx.coroutines.MainScope().launch {
            requestDoorOpen()
        }
    }
}