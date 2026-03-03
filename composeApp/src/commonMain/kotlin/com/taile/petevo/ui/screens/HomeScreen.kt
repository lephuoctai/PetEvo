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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.solid.Fire
import com.composables.icons.fontawesome.solid.Frown
import com.composables.icons.fontawesome.solid.FrownOpen
import com.composables.icons.fontawesome.solid.HourglassHalf
import com.composables.icons.fontawesome.solid.Paw
import com.composables.icons.fontawesome.solid.Smile
import com.composables.icons.fontawesome.solid.Stopwatch
import com.taile.petevo.engine.FocusUiState
import com.taile.petevo.logic.LevelSystem
import com.taile.petevo.model.SessionState
import com.taile.petevo.ui.theme.*

@Composable
fun HomeScreen(
    state: FocusUiState,
    onStartSetup: () -> Unit
) {
    val pet = state.pet
    val eColor = emotionColor(pet.emotion)
    val eLabel = emotionLabel(pet.emotion)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: Pet info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FocusPet",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pet Avatar
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(eColor.copy(alpha = 0.6f), eColor.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    petIcon(pet.emotion),
                    contentDescription = "Pet",
                    modifier = Modifier.size(72.dp),
                    colorFilter = ColorFilter.tint(eColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = eLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = eColor
            )
        }

        // Middle: Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLight)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level
            Text(
                text = "Level ${pet.level}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // XP Progress
            val xpNeeded = LevelSystem.xpForNextLevel(pet.level)
            val progress = if (xpNeeded > 0) pet.xp.toFloat() / xpNeeded.toFloat() else 0f

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = PrimaryGreen,
                trackColor = Color.LightGray,
            )

            Text(
                text = "${pet.xp} / $xpNeeded XP",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emotion bar
            Text(
                text = "Emotion",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                val emotionFraction = ((pet.emotion + 10).toFloat() / 109f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(emotionFraction)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(EmotionVerySad, EmotionSad, EmotionNeutral, EmotionHappy)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(FontAwesome.Solid.Fire, "${pet.streak}", "Streak", AccentOrange)
                StatChip(FontAwesome.Solid.Stopwatch, "${pet.totalFocusMinutes}m", "Total Focus", PrimaryGreen)
            }
        }

        // Bottom: Action button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.sessionState == SessionState.COOLDOWN) {
                val mins = state.cooldownRemainingSeconds / 60
                val secs = state.cooldownRemainingSeconds % 60
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        FontAwesome.Solid.HourglassHalf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(AccentRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cooldown: ${mins}m ${secs}s",
                        fontSize = 16.sp,
                        color = AccentRed,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onStartSetup,
                enabled = state.sessionState == SessionState.IDLE,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = if (state.sessionState == SessionState.COOLDOWN) "On Cooldown..." else "Start Focus Session",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(tint)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
        }
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

private fun petIcon(emotion: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        emotion >= 87 -> FontAwesome.Solid.Smile
        emotion >= 50 -> FontAwesome.Solid.Paw
        emotion >= 0 -> FontAwesome.Solid.Frown
        else -> FontAwesome.Solid.FrownOpen
    }
}
