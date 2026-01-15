package com.idsolution.icondoapp.feature.mainscreen

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
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import com.idsolution.icondoapp.feature.auth.domain.AuthState
import com.idsolution.icondoapp.feature.auth.presentation.createuser.SignupScreenRoot
import com.idsolution.icondoapp.feature.auth.presentation.intro.IntroScreenRoot
import com.idsolution.icondoapp.feature.auth.presentation.login.LoginScreenRoot
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun NavigationRoot(
    onIncomingCall: ((String) -> Unit),
    onErrorLogin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authSessionManager: AuthSessionManager = koinInject()
    val authState by authSessionManager.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Check auth state on startup
    LaunchedEffect(Unit) {
        authSessionManager.checkAuthState()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                navController.navigate("intro") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Authenticated -> {
                navController.navigate("mainscreen") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
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
                authGraph(
                    navController = navController,
                    onIncomingCall = { onIncomingCall.invoke(it) },
                    onErrorLogin = {
                        onErrorLogin.invoke(it)
                    },
                    onLogout = {
                        coroutineScope.launch {
                            authSessionManager.logout()
                        }
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onIncomingCall: (String) -> Unit,
    onErrorLogin: (String) -> Unit,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = "intro",
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
                },
                onErrorLogin = {
                    onErrorLogin.invoke(it)
                },
                onBackClick = {
                    navController.navigate("intro")
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
            MainScreen(onLogout = { onLogout.invoke() })
        }
    }
}
