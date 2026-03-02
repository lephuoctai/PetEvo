package com.taile.petevo.model

data class Pet(
    val level: Int = 1,
    val xp: Int = 0,
    val emotion: Int = 50, // Range -10 to 99
    val totalFocusMinutes: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: Long = 0L
) {
    val clampedEmotion: Int get() = emotion.coerceIn(-10, 99)
}

