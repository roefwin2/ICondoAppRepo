package com.example.testkmpapp.feature.mainscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testkmpapp.feature.ssh.presenter.places.PlacesScreen
import com.idsolution.icondoapp.feature.mainscreen.MainViewModel
import com.idsolution.icondoapp.feature.voip.NativeVoipScreen
import com.idsolution.icondoapp.feature.voip.VoipViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    onIncomingCall: ((String) -> Unit),
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }


    val mainState = mainViewModel.state.value
    val username = mainState.username

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { CustomTopAppBar(title = getScreenTitle(selectedTab), username = username) },
        bottomBar = { BottomNavigationBar(selectedTab) { selectedTab = it } }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Content based on selected tab
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (selectedTab) {
                    0 -> {
                        PlacesScreen()
                    }

                    1 -> {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "voip") {
                            composable("voip") {
                                val voipViewModel: VoipViewModel = koinViewModel()
                                val state = voipViewModel.state.collectAsState().value
                                LaunchedEffect(Unit){
                                    voipViewModel.getPhonebook()
                                }
                                LaunchedEffect(state){
                                    if(state is com.idsolution.icondoapp.core.data.networking.Result.Error){
                                        snackbarHostState.showSnackbar(state.error.toString())
                                    }
                                }
                                val listPhoneBook = if(state is com.idsolution.icondoapp.core.data.networking.Result.Success) {
                                    state.data
                                } else {
                                    emptyList()
                                }
                                println("listPhoneBook: $state")
                                NativeVoipScreen(phoneBook = listPhoneBook)
                            }
                        }
                    }

                    2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { onLogout.invoke() }) {
                                Text("Deconnexion")
                            }
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(title: String, username: String) {
    TopAppBar(
        colors = TopAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Column {
                Text(text = "Salut $username", fontSize = 12.sp, color = Color.Gray)
                Text(text = title, fontSize = 18.sp)
            }
        },
        navigationIcon = {
            Image(
                imageVector = Icons.Rounded.AccountCircle, // Remplacez par l'image souhaitée
                contentDescription = "User Image",
                modifier = Modifier.size(40.dp)
            )
        },
        actions = {
            IconButton(onClick = { /* Action de la cloche */ }) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications")
            }
        }
    )
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
            label = { Text("Sites Controller") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Build, contentDescription = "Documents") },
            label = { Text("Calling") },
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Partager") },
            label = { Text("Vidéo") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}

fun getScreenTitle(index: Int): String {
    return when (index) {
        0 -> "Site Controller"
        1 -> "Calling"
        2 -> "Vidéo"
        else -> ""
    }
}