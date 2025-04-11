package com.idsolution.icondoapp.feature.voip

import androidx.compose.runtime.Composable
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook

@Composable
expect fun NativeVoipScreen(phoneBook: List<PhoneBook>)