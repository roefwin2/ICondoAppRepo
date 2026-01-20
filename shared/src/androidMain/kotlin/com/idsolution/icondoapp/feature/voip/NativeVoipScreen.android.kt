package com.idsolution.icondoapp.feature.voip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

/**
 * Android implementation of NativeVoipScreen
 * This screen will display VoIP contacts and allow making calls
 *
 * TODO: Implement proper VoIP call UI using VoipService
 */
@Composable
actual fun NativeVoipScreen(phoneBook: List<PhoneBook>) {
    if (phoneBook.isNotEmpty()) {
        // TODO: Implement proper VoIP contacts screen
        // For now, just show a placeholder
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("VoIP Contacts: ${phoneBook.size} contacts available")
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No PhoneBook available")
        }
    }
}
