package com.taile.petevo.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
val PrimaryGreen = Color(0xFF4CAF50)
val DarkGreen = Color(0xFF2E7D32)
val LightGreen = Color(0xFFA5D6A7)

// Emotion colors
val EmotionHappy = Color(0xFFFFD54F)    // Gold/Yellow
val EmotionNeutral = Color(0xFF81C784)  // Green
val EmotionSad = Color(0xFF64B5F6)      // Blue
val EmotionVerySad = Color(0xFFE57373)  // Red

// Background
val BackgroundLight = Color(0xFFF5F5F5)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF212121)

// Accent
val AccentOrange = Color(0xFFFF9800)
val AccentRed = Color(0xFFF44336)

fun emotionColor(emotion: Int): Color {
    return when {
        emotion >= 87 -> EmotionHappy
        emotion >= 50 -> EmotionNeutral
        emotion >= 0 -> EmotionSad
        else -> EmotionVerySad
    }
}

fun emotionLabel(emotion: Int): String {
    return when {
        emotion >= 87 -> "Happy"
        emotion >= 80 -> "Content"
        emotion >= 50 -> "Neutral"
        emotion >= 0 -> "Sad"
        else -> "Very Sad"
    }
}

