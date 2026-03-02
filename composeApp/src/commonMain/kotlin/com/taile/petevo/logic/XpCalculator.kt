package com.taile.petevo.logic

import com.taile.petevo.model.FocusMode
import kotlin.math.pow
import kotlin.math.roundToInt

object XpCalculator {

    /**
     * Deep Focus (High Risk): y = ((x / 30)^2 + 0.8) * 100
     * Pomodoro (Stable): y = 1.5 * x
     * x = duration in minutes (minimum 10)
     */
    fun calculateXp(mode: FocusMode, durationMinutes: Int): Int {
        val minutes = durationMinutes.coerceAtLeast(10)
        return when (mode) {
            FocusMode.DEEP_FOCUS -> {
                val x = minutes.toDouble()
                (((x / 30.0).pow(2) + 0.8) * 100.0).roundToInt()
            }
            FocusMode.POMODORO -> {
                (1.5 * minutes).roundToInt()
            }
        }
    }
}

