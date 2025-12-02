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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.absoluteValue

@OptIn(KoinExperimentalAPI::class)
@Composable
fun PlacesScreen(
    viewModel: CondoSitesViewModel = koinViewModel(),
    onNavigateToCamera: (siteId: Int, siteName: String) -> Unit = { _, _ -> } // ✅ Ajout callback navigation
) {
    val state = viewModel.state.collectAsState().value

    when (val res = state.sites) {
        is Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Success -> {
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
                        PlaceCard(
                            site = site,
                            onDoorChange = { open, doorNumber ->
                                viewModel.onDoorChange(
                                    condoSite = site,
                                    doorNumber = doorNumber,
                                    open = open
                                )
                            },
                            onCameraClick = {
                                // ✅ Navigation vers les caméras
                                onNavigateToCamera(site.siteId, site.siteName)
                            }
                        )
                    }
                }
            }
        }

        is com.idsolution.icondoapp.core.presentation.helper.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Une erreur est survenue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.errorCause ?: "Erreur inconnue",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        is Idle -> {}
    }
}

@Composable
fun PlaceCard(
    site: CondoSite,
    onDoorChange: (Boolean, Int) -> Unit,
    onCameraClick: () -> Unit // ✅ Ajout callback caméra
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ✅ En-tête avec nom du site et bouton caméras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.siteName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Site #${site.siteId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ✅ Bouton Caméras
                FilledTonalButton(
                    onClick = onCameraClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("📹")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Caméras")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Séparateur
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section Portes
            Text(
                text = "Portes (${site.doors.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Liste des portes
            site.doors.forEach { door ->
                DoorItem(door) { open ->
                    onDoorChange.invoke(open, door.number)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "🚪",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = door.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        CustomSwitchWithLoading(
            isChecked = isChecked,
            isLoading = door.isOpen is Loading<*>
        ) {
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
    var localIsChecked by remember { mutableStateOf(isChecked) }
    var remainingTimeMs by remember { mutableStateOf(0L) }
    val maxTimeMs = 5000L

    val position by animateFloatAsState(
        targetValue = if (localIsChecked) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "switchPosition"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (localIsChecked) Color.Green else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    LaunchedEffect(isChecked) {
        localIsChecked = isChecked
        if (isChecked) {
            remainingTimeMs = maxTimeMs
            while (remainingTimeMs > 0) {
                kotlinx.coroutines.delay(16)
                remainingTimeMs -= 16
            }
            localIsChecked = false
            onCheckedChange(false)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (localIsChecked) "OPEN" else "CLOSE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (localIsChecked) Color.Green else Color.Gray
        )

        if (localIsChecked && !isLoading) {
            LinearProgressIndicator(
                progress = { 1f - (remainingTimeMs.toFloat() / maxTimeMs) },
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
        // Preview
    }
}