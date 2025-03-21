package com.idsolution.icondoapp.core.presentation.helper


sealed class Resource<out T>(open val value: T?)
data class  Idle<T>(override var value: T? = null) : Resource<T>(value)
data class Success<T>(override val value: T) : Resource<T>(value)
data class Loading<T>(override var value: T? = null) : Resource<T>(value)
class Error<out T>(val errorCause : String) : Resource<T>(null)