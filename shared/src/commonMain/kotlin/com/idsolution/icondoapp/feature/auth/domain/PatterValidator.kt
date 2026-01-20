package com.idsolution.icondoapp.feature.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}