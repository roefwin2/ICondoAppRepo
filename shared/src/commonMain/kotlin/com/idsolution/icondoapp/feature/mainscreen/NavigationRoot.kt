package com.example.testkmpapp.feature.mainscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.testkmpapp.feature.auth.presentation.intro.IntroScreenRoot
import com.example.testkmpapp.feature.auth.presentation.login.LoginScreenRoot

@Composable
fun NavigationRoot(
    onIncomingCall: ((String) -> Unit),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(modifier = modifier, navController = navController, startDestination = "auth") {
        authGGraph(navController, onIncomingCall = { onIncomingCall.invoke(it) })
    }
}

private fun NavGraphBuilder.authGGraph(
    navController: NavHostController,
    onIncomingCall: (String) -> Unit
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
                onSignUpClick = {}
            )
        }
        composable(route = "login") {
            LoginScreenRoot(
                onSignUpClick = {},
                onLoginSuccess = {
                    navController.navigate("mainscreen")
                }
            )
        }
        composable("mainscreen") {
            MainScreen(onIncomingCall = {
                onIncomingCall.invoke(it)
            })
        }
    }
}