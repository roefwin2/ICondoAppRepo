package com.example.voip.voip.presenter.contacts

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voip.voip.presenter.TextureViewScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsScreen(viewModel: ContactsViewModel = koinViewModel(), onCallClick: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Entrez un numéro") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onCallClick.invoke(phoneNumber)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = phoneNumber.isNotBlank()
        ) {
            Text("Appeler")
        }

        LazyColumn {
            items(viewModel.contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onCallClick = {
                        onCallClick.invoke("")
                        viewModel.callNumber("")
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = SpaceBetween
        ) {
            Text(text = "${contact.name} (${contact.number})")
            IconButton(onClick = onCallClick) {
                Icon(Icons.Rounded.Phone, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    MaterialTheme {
        ContactsScreen {}
    }
}

@Preview(showBackground = true)
@Composable
fun ContactItemPreview() {
    MaterialTheme {
        ContactItem(
            contact = Contact("Contact Preview", "0123456789"),
            onCallClick = {}
        )
    }
}