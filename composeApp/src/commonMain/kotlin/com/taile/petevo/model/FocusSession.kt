package com.taile.petevo.model

data class FocusSession(
    val mode: FocusMode = FocusMode.POMODORO,
    val durationMinutes: Int = 25,
    val startTimeMs: Long = 0L,
    val projectedXp: Int = 0,
    val remainingSeconds: Int = 0
)

