package com.example.testkmpapp.feature.ssh.presenter.sites


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CondoSitesRoot(
    viewModel: CondoSitesViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    onNavigate: (() -> Unit)
) {
//    val doorOpened = viewModel.state.doorOpened
//    val sites = viewModel.state.sites
//    val context = LocalContext.current
//    LaunchedEffect(doorOpened) {
//        if (doorOpened) {
//            Toast.makeText(context, "Door opened", Toast.LENGTH_LONG).show()
//        }
//    }
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Sites controller", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
//        Spacer(modifier = Modifier.height(12.dp))
//        LazyColumn {
//            items(sites) { site ->
//                SiteItem(condoSite = site) { door ->
//                    viewModel.openDoor(condoSite = site, door)
//                }
//            }
//        }
    OutlinedButton(onClick = {
        onNavigate.invoke()
    }) {
        Text("Go to Call")
    }
}

@Composable
fun SiteItem(condoSite: CondoSite, onOpenDoor: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(8.dp)
            .border(2.dp, color = MaterialTheme.colorScheme.onBackground, RoundedCornerShape(8.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section with Icon and Site Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home, // Change this icon as per your need
                    contentDescription = "Site Icon",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = condoSite.siteName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun DoorButton(doorNumber: Int, onOpenDoor: () -> Unit) {
    // State to track whether the door is opened or not
    var isOpen by remember { mutableStateOf(false) }

    // Animation for rotation (from 0 to 90 degrees)
    val rotation by animateFloatAsState(targetValue = if (isOpen) 360f else 0f, label = "")

    // Button for the door with rotation animation
    Button(
        onClick = {
            isOpen = !isOpen
            onOpenDoor()
        },
        modifier = Modifier
            .graphicsLayer(rotationZ = rotation) // Apply rotation animation
            .padding(8.dp)
    ) {
        Text(text = "Door $doorNumber")
    }
}
