package com.taile.petevo.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.solid.ArrowRight
import com.composables.icons.fontawesome.solid.ArrowUp
import com.composables.icons.fontawesome.solid.Frown
import com.composables.icons.fontawesome.solid.HeartBroken
import com.composables.icons.fontawesome.solid.HourglassHalf
import com.composables.icons.fontawesome.solid.Paw
import com.composables.icons.fontawesome.solid.Smile
import com.composables.icons.fontawesome.solid.Trophy
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
            Image(
                if (isSuccess) FontAwesome.Solid.Trophy else FontAwesome.Solid.HeartBroken,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                colorFilter = ColorFilter.tint(if (isSuccess) EmotionHappy else AccentRed)
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
            // Pet icon
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
                Image(
                    if (isSuccess) FontAwesome.Solid.Smile else FontAwesome.Solid.Frown,
                    contentDescription = "Pet",
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(if (isSuccess) EmotionHappy else AccentRed)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        FontAwesome.Solid.ArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(EmotionHappy)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LEVEL UP! ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmotionHappy
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Last Level
                    Text(
                        text = "${state.prevLevel}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        FontAwesome.Solid.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    //New Level
                    Text(
                        text = state.pet.level.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmotionHappy
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Emotion change
            Text(
                text = "Emotion:${state.pet.emotion}",
                fontSize = 16.sp,
                color = emotionColor(state.pet.emotion)
            )

            if (!isSuccess) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        FontAwesome.Solid.HourglassHalf,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(AccentOrange)
                    )
                }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSuccess) "Continue" else "OK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        if (isSuccess) FontAwesome.Solid.Paw else FontAwesome.Solid.Frown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

