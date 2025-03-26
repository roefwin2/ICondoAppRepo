package com.example.voip.voip.utils

import org.linphone.core.Call

fun Call.State.isIncomingState() : Boolean{
    return this == Call.State.IncomingReceived
            || this == Call.State.PushIncomingReceived
            || this == Call.State.IncomingEarlyMedia
}