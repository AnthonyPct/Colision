package com.anthooop.colision

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform