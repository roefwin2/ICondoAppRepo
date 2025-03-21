package com.idsolution.icondoapp.core.presentation.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CondoActionButton(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    onClick: (() -> Unit)
) {
    Button(
        onClick = onClick, enabled = enable, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.Black
        ),
        shape = RoundedCornerShape(100f),
        modifier = Modifier.height(IntrinsicSize.Min) // Take le min height for the smaller child => Not take all the MaxHeight of one child
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(15.dp)
                    .alpha(if (isLoading) 1f else 0f),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = text,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f), // not remove text because button can shrink
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CondoOutlinedActionButton(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    onClick: (() -> Unit)
) {
    Button(
        onClick = onClick, enabled = enable, colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(100f),
        modifier = Modifier.height(IntrinsicSize.Min) // Take le min height for the smaller child => Not take all the MaxHeight of one child
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(15.dp)
                    .alpha(if (isLoading) 1f else 0f),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = text,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f), // not remove text because button can shrink
                fontWeight = FontWeight.Medium
            )
        }
    }
}