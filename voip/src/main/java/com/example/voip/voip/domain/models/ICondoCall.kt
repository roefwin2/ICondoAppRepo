package com.example.voip.voip.domain.models

import org.linphone.core.Call

data class ICondoCall(
    val call : Call? = null,
    val state : Call.State = Call.State.Idle
)
