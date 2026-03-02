package com.taile.petevo.logic

import com.taile.petevo.model.FocusMode

object EmotionEngine {

    /**
     * Returns the XP multiplier based on current emotion value.
     * 87..99  -> 1.50 (Happy)
     * 80..86  -> 1.10 (Neutral+)
     * 50..79  -> 0.95 (Neutral)
     * 0..49   -> 0.80 (Sad)
     * -10..-1 -> -1.10 (Very Sad — XP penalty!)
     */
    fun getMultiplier(emotion: Int): Double {
        val clamped = emotion.coerceIn(-10, 99)
        return when {
            clamped in 87..99 -> 1.50
            clamped in 80..86 -> 1.10
            clamped in 50..79 -> 0.95
            clamped in 0..49 -> 0.80
            else -> -1.10 // -10 to -1
        }
    }

    /**
     * Emotion delta on session success.
     * Deep Focus: +8, Pomodoro: +5
     */
    fun successDelta(mode: FocusMode): Int = when (mode) {
        FocusMode.DEEP_FOCUS -> 8
        FocusMode.POMODORO -> 5
    }

    /**
     * Emotion delta on session failure.
     * Deep Focus: -15, Pomodoro: -8
     */
    fun failureDelta(mode: FocusMode): Int = when (mode) {
        FocusMode.DEEP_FOCUS -> -15
        FocusMode.POMODORO -> -8
    }

    /**
     * Apply emotion change and clamp to valid range.
     */
    fun applyDelta(currentEmotion: Int, delta: Int): Int {
        return (currentEmotion + delta).coerceIn(-10, 99)
    }
}

