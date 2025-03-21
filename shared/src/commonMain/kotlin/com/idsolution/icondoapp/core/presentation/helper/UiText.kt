package com.idsolution.icondoapp.core.presentation.helper

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        val id: Int,
        val args: Array<Any> = arrayOf()
    ) : UiText

    @OptIn(InternalResourceApi::class)
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resource = org.jetbrains.compose.resources.StringResource(
                id.toString(),"",
                setOf()
            ), *args)
        }
    }
}