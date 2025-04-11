package com.example.testkmpapp.feature.auth.presentation.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idsolution.icondoapp.core.presentation.designsystem.component.CondoActionButton
import com.idsolution.icondoapp.core.presentation.designsystem.component.CondoPasswordTextField
import com.idsolution.icondoapp.core.presentation.designsystem.component.CondoTextField
import com.idsolution.icondoapp.core.presentation.designsystem.component.GradientBackground
import com.idsolution.icondoapp.core.presentation.helper.ObserveAsEvents
import com.example.testkmpapp.theme.CondoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun LoginScreenRoot(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onErrorLogin: (String) -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect {
            when (it) {
                is LoginEvent.Error -> {
                    onErrorLogin.invoke(it.error.toString())
                }
                else -> Unit
            }
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.Error -> {
                keyboardController?.hide()
            }

            LoginEvent.LoginSuccess -> {
                keyboardController?.hide()
                onLoginSuccess()
            }
        }
    }
    LoginScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                is LoginAction.OnRegisterClick -> onSignUpClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(vertical = 32.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Hi there",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Welcome",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            CondoTextField(
                state = state.email,
                startIcon = Icons.Default.Email,
                endIcon = null,
                hint = "Email",
                title = "Email",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            CondoPasswordTextField(
                state = state.password,
                isPasswordVisible = state.isPasswordVisible,
                onTogglePasswordVisibility = {
                    onAction(LoginAction.OnTogglePasswordVisibility)
                },
                hint = "Password",
                title = "Password",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            CondoActionButton(
                text = "Login",
                isLoading = state.isLoggingIn,
                enable = state.canLogin,
                onClick = {
                    onAction(LoginAction.OnLoginClick)
                },
            )

            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    append("Register")
                    pushStringAnnotation(
                        tag = "clickable_text",
                        annotation = "Register"
                    )
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    ) {
                        append("Register")
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "clickable_text",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            onAction(LoginAction.OnRegisterClick)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun LoginScreenPreview() {
    CondoTheme {
        LoginScreen(
            state = LoginState(),
            onAction = {}
        )
    }
}
