package com.example.testkmpapp.feature.auth.presentation.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idsolution.icondoapp.core.presentation.designsystem.component.CondoActionButton
import com.idsolution.icondoapp.core.presentation.designsystem.component.CondoOutlinedActionButton
import com.idsolution.icondoapp.core.presentation.designsystem.component.GradientBackground
import com.example.testkmpapp.theme.CondoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IntroScreenRoot(
    onSignInClick: (() -> Unit),
    onSignUpClick: (() -> Unit),
) {
    IntroScreen(
        onAction = { action ->
            when (action) {
                IntroAction.OnSignInClick -> onSignInClick.invoke()
                IntroAction.OnSignUpClick -> onSignUpClick.invoke()
            }

        }
    )
}

@Composable
fun IntroScreen(
    onAction: ((IntroAction) -> Unit)
) {
    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CondoLogoVertical()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Welcome to ICondo",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "A Controller App", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            CondoActionButton(
                text = "Sign up",
                isLoading = false,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    onAction.invoke(IntroAction.OnSignUpClick)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CondoOutlinedActionButton(
                text = "Sign in",
                isLoading = false,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    onAction.invoke(IntroAction.OnSignInClick)
                }
            )
        }
    }
}

@Composable
private fun CondoLogoVertical(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Rounded.Face,
            contentDescription = "logo",
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ICondo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
private fun IntroScreenPreview() {
    CondoTheme {
        IntroScreen { }
    }
}