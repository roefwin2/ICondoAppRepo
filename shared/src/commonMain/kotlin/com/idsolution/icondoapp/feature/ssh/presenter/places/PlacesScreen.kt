package com.example.testkmpapp.feature.ssh.presenter.places

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.idsolution.icondoapp.core.presentation.helper.Loading
import com.idsolution.icondoapp.core.presentation.helper.Success
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.example.testkmpapp.feature.ssh.domain.models.Door
import com.example.testkmpapp.feature.ssh.presenter.sites.CondoSitesViewModel
import com.idsolution.icondoapp.core.data.networking.Error
import com.idsolution.icondoapp.core.presentation.helper.Idle
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun PlacesScreen(
    viewModel: CondoSitesViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsState().value

    when (val res = state.sites) {
        is Loading -> {
            CircularProgressIndicator()
        }

        is Success -> {
            // Afficher votre contenu principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(res.value) { site ->
                        PlaceCard(site) { open, doorNumber ->
                            viewModel.onDoorChange(
                                condoSite = site,
                                doorNumber = doorNumber,
                                open = open
                            )
                        }
                    }
                }
            }
        }

        is com.idsolution.icondoapp.core.presentation.helper.Error -> {
            // Afficher votre message d'erreur
            Text(
                text = "Une erreur est survenue: ${res.errorCause}",
                color = MaterialTheme.colorScheme.error
            )
        }

        is Idle -> {}
    }
}

@Composable
fun PlaceCard(site: CondoSite, onChecked: (Boolean, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = site.siteName,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            site.doors.forEach { door ->
                DoorItem(door) { open ->
                    onChecked.invoke(open, door.number)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DoorItem(door: Door, onChecked: (Boolean) -> Unit) {
    val isChecked = door.isOpen is Success<*> && door.isOpen.value == true
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = SpaceBetween
    ) {
        Text(text = door.name)
        CustomSwitchWithLoading(isChecked = isChecked, isLoading = door.isOpen is Loading<*>) {
            onChecked.invoke(it)
        }
    }
}

@Composable
fun CustomSwitchWithLoading(
    isChecked: Boolean,
    isLoading: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = if (isChecked) "OPEN" else "CLOSE")

        Box(
            modifier = Modifier
                .size(50.dp, 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (isChecked) Color.Green else Color.Gray)
                .clickable(enabled = !isLoading) {
                    onCheckedChange(!isChecked)
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(
                            if (isChecked) Alignment.CenterEnd else Alignment.CenterStart
                        )
                        .padding(horizontal = 5.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun PlaceCardPreview() {
    MaterialTheme {

    }
}

@Preview
@Composable
fun DoorItemPreview() {
    MaterialTheme {
        DoorItem(Door("Porte Preview", Success(true), 5)) {}
    }
}