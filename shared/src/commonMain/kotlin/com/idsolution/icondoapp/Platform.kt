package com.idsolution.icondoapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform