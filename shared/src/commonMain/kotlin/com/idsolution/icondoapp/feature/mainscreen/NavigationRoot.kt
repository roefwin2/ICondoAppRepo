package com.example.testkmpapp.feature.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.testkmpapp.feature.auth.presentation.intro.IntroScreenRoot
import com.example.testkmpapp.feature.auth.presentation.login.LoginScreenRoot
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import com.idsolution.icondoapp.feature.auth.domain.AuthState
import com.idsolution.icondoapp.feature.auth.presentation.createuser.SignupScreenRoot
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun NavigationRoot(
    onIncomingCall: ((String) -> Unit),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authSessionManager: AuthSessionManager = koinInject()
    val authState by authSessionManager.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Vérifier l'état d'authentification au démarrage
    LaunchedEffect(Unit) {
        authSessionManager.checkAuthState()
    }

    when (authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        else -> {
            NavHost(
                modifier = modifier,
                navController = navController,
                startDestination = "auth"
            ) {
                authGGraph(
                    startDestination = if (authState is AuthState.Authenticated) "mainscreen" else "intro",
                    navController = navController,
                    onIncomingCall = { onIncomingCall.invoke(it) },
                    onLogout = {
                        // Déconnexion
                        coroutineScope.launch {
                            authSessionManager.logout()
                            navController.navigate("auth") {
                                popUpTo("mainscreen") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.authGGraph(
    startDestination: String,
    navController: NavHostController,
    onIncomingCall: (String) -> Unit,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = startDestination,
        route = "auth"
    ) {
        composable(route = "intro") {
            IntroScreenRoot(
                onSignInClick = {
                    navController.navigate("login")
                },
                onSignUpClick = {
                    navController.navigate("signup")
                }
            )
        }
        composable(route = "login") {
            LoginScreenRoot(
                onSignUpClick = {
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("mainscreen")
                }
            )
        }
        composable(route = "signup") {
            SignupScreenRoot(
                onSignupSuccess = {
                    navController.navigate("mainscreen")
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }
        composable("mainscreen") {
            MainScreen(
                onIncomingCall = {
                    onIncomingCall.invoke(it)
                }, onLogout =
                { onLogout.invoke() })
        }
    }
}