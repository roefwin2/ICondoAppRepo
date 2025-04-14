package com.example.testkmpapp.feature.ssh.presenter.places

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorStatus
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.math.absoluteValue

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
    // État interne pour suivre la transition
    var localIsChecked by remember { mutableStateOf(isChecked) }
    var remainingTimeMs by remember { mutableStateOf(0L) }
    val maxTimeMs = 5000L // 5 secondes

    // Animation de la position du curseur
    val position by animateFloatAsState(
        targetValue = if (localIsChecked) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "switchPosition"
    )

    // Animation de la couleur de fond
    val backgroundColor by animateColorAsState(
        targetValue = if (localIsChecked) Color.Green else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    // Effet pour synchroniser l'état local avec l'état externe
    LaunchedEffect(isChecked) {
        localIsChecked = isChecked
        if (isChecked) {
            remainingTimeMs = maxTimeMs
            // Lancer le compte à rebours
            while (remainingTimeMs > 0) {
                delay(16) // Rafraîchissement environ 60 fois par seconde
                remainingTimeMs -= 16
            }
            // Après 5 secondes, déclencher la désélection
            localIsChecked = false
            onCheckedChange(false)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = if (localIsChecked) "OPEN" else "CLOSE")

        // Indicateur de progression (optionnel)
        if (localIsChecked && !isLoading) {
            LinearProgressIndicator(
                progress = 1f - (remainingTimeMs.toFloat() / maxTimeMs),
                modifier = Modifier.width(30.dp).height(2.dp),
                color = Color.Red
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .size(50.dp, 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(backgroundColor)
                .clickable(enabled = !isLoading) {
                    onCheckedChange(!localIsChecked)
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
                        .align(Alignment.Center)
                        .offset(
                            x = ((if (position > 0.5f) 15.dp else -15.dp) * (2 * position - 1).absoluteValue)
                        )
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
        //DoorItem(Door("Porte Preview", Success(true), 5)) {}
    }
}