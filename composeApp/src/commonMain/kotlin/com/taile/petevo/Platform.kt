package com.taile.petevo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform