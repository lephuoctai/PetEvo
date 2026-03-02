package com.taile.petevo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taile.petevo.model.FocusMode
import com.taile.petevo.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SetupScreen(
    previewXp: (FocusMode, Int) -> Int,
    onStart: (FocusMode, Int) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(FocusMode.POMODORO) }
    var durationMinutes by remember { mutableStateOf(25f) }

    val projectedXp = previewXp(mode, durationMinutes.roundToInt())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Setup Session",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mode toggle
            Text(
                text = "Focus Mode",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeCard(
                    title = "🍅 Pomodoro",
                    subtitle = "Stable XP gain",
                    selected = mode == FocusMode.POMODORO,
                    onClick = { mode = FocusMode.POMODORO },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "🧠 Deep Focus",
                    subtitle = "High risk, high reward",
                    selected = mode == FocusMode.DEEP_FOCUS,
                    onClick = { mode = FocusMode.DEEP_FOCUS },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Duration slider
            Text(
                text = "Duration: ${durationMinutes.roundToInt()} minutes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                valueRange = 10f..180f,
                steps = 33, // 10, 15, 20, ... 180
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryGreen,
                    activeTrackColor = PrimaryGreen,
                    inactiveTrackColor = LightGreen
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10 min", fontSize = 12.sp, color = Color.Gray)
                Text("180 min", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // XP Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceLight)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Projected XP",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (projectedXp >= 0) "+$projectedXp" else "$projectedXp",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (projectedXp >= 0) PrimaryGreen else AccentRed
                    )
                    if (projectedXp < 0) {
                        Text(
                            text = "⚠️ Your pet is very sad! XP will be negative.",
                            fontSize = 12.sp,
                            color = AccentRed,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom buttons
        Column {
            Button(
                onClick = { onStart(mode, durationMinutes.roundToInt()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = "🚀 Begin Focus",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Back", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) PrimaryGreen.copy(alpha = 0.15f) else SurfaceLight
    val borderColor = if (selected) PrimaryGreen else Color.LightGray

    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.outlinedCardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) DarkGreen else OnSurfaceLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}


