package com.example.testkmpapp.feature.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}