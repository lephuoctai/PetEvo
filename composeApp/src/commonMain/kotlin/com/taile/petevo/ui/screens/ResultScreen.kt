package com.taile.petevo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taile.petevo.engine.FocusUiState
import com.taile.petevo.model.SessionState
import com.taile.petevo.ui.theme.*

@Composable
fun ResultScreen(
    state: FocusUiState,
    onDismiss: () -> Unit
) {
    val isSuccess = state.sessionState == SessionState.SUCCESS
    val bgColor = if (isSuccess) Color(0xFF1B5E20) else Color(0xFF4A0000)
    val accentColor = if (isSuccess) PrimaryGreen else AccentRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top: Result header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSuccess) "🎉" else "💔",
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSuccess) "Session Complete!" else "Session Failed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSuccess)
                    "Great job! Your pet is proud of you!"
                else
                    "You left the app. Your pet is disappointed...",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Middle: Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pet emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSuccess) "😺" else "😿",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // XP gained
            Text(
                text = if (isSuccess) "XP +${state.lastXpGained}" else "XP 0",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            if (!isSuccess) {
                Text(
                    text = "(No XP for leaving!)",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Level
            if (state.didLevelUp) {
                Text(
                    text = "🎊 LEVEL UP! → Level ${state.pet.level}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmotionHappy
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Level ${state.pet.level}",
                fontSize = 18.sp,
                color = Color.White
            )

            // Emotion change
            Text(
                text = "Emotion: ${emotionLabel(state.pet.emotion)}",
                fontSize = 16.sp,
                color = emotionColor(state.pet.emotion)
            )

            if (!isSuccess) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⏳ 5 minute cooldown active",
                    fontSize = 14.sp,
                    color = AccentOrange,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom: Dismiss
        Column {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = if (isSuccess) "Continue 🐾" else "OK 😔",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

