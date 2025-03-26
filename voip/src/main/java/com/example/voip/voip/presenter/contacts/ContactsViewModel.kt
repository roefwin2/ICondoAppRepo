package com.example.voip.voip.presenter.contacts

import androidx.lifecycle.ViewModel
import com.example.voip.voip.domain.ICondoVoip
import org.linphone.core.TransportType

data class Contact(
    val name: String,
    val number: String
)

class ContactsViewModel(
    private val voip: ICondoVoip
) : ViewModel() {
    init {
        voip.login(
            username = "regis_test",
            password = "e1d2o3U4",
            domain = "sip.linphone.org", transportType = TransportType.Tcp
        )
    }

    fun callNumber(number: String) {
        voip.outgoingCall("sip:$number@sip.linphone.org")
    }
}