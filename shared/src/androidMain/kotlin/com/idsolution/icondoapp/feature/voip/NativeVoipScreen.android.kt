package com.idsolution.icondoapp.feature.voip

import android.telecom.Call
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voip.voip.presenter.call.CallScreen
import com.example.voip.voip.presenter.call.CallScreenRoot
import com.example.voip.voip.presenter.contacts.Contact
import com.example.voip.voip.presenter.contacts.ContactsScreen
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.data.networking.map
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun NativeVoipScreen() {
    val voipViewModel: VoipViewModel = koinViewModel()
    val state = voipViewModel.state.collectAsState().value
    val navController = rememberNavController()
    if (state is Result.Success) {
        val contacts = state.data
        NavHost(navController = navController, startDestination = "contacts") {
            composable("contacts") {
                ContactsScreen(contacts = contacts.map { Contact(it.firstname, it.phone) }) {
                    navController.navigate("calling")
                }
            }
            composable("calling") {
                CallScreenRoot(onIncomingCall = {

                }, onEndCall = {
                    navController.navigate("contacts")
                })
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pas de PhoneBook disponible")
        }
    }
}