package com.example.voip.voip.presenter.contacts


import androidx.lifecycle.ViewModel
import com.example.voip.voip.domain.ICondoVoip

data class Contact(
    val name: String,
    val number: String
)

class ContactsViewModel(
    private val voip: ICondoVoip
) : ViewModel() {

    fun callNumber(number: String) {
        voip.outgoingCall("sip:$number@31.97.155.55")
    }

    fun logout() {
        voip.logout()
    }
}