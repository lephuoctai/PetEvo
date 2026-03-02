package com.taile.petevo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taile.petevo.engine.FocusUiState
import com.taile.petevo.model.FocusMode
import com.taile.petevo.ui.theme.*

@Composable
fun FocusScreen(
    state: FocusUiState,
    onCancel: () -> Unit
) {
    val session = state.session
    val totalSeconds = session.durationMinutes * 60
    val remaining = session.remainingSeconds
    val progress = if (totalSeconds > 0) 1f - (remaining.toFloat() / totalSeconds) else 0f

    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: Mode label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (session.mode) {
                    FocusMode.DEEP_FOCUS -> "🧠 Deep Focus"
                    FocusMode.POMODORO -> "🍅 Pomodoro"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Text(
                text = "Stay focused! Don't leave this tab.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }

        // Center: Circular timer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            // Circular progress background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Track
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                val sweepAngle = progress * 360f
                drawArc(
                    color = PrimaryGreen,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Time text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = petFocusEmoji(state.pet.emotion),
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "+${session.projectedXp} XP",
                    fontSize = 16.sp,
                    color = PrimaryGreen.copy(alpha = 0.8f)
                )
            }
        }

        // Bottom: Cancel button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentRed
                )
            ) {
                Text("⚠️ Abandon Session", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Two-step emergency cancel dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Abandon your pet?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to quit? Your pet will be sad! " +
                    "You'll get 0 XP, an emotion penalty, and a 5-minute cooldown."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRed)
                ) {
                    Text("Yes, abandon", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep going! 💪")
                }
            }
        )
    }
}

private fun petFocusEmoji(emotion: Int): String {
    return when {
        emotion >= 87 -> "😺"
        emotion >= 50 -> "🐱"
        emotion >= 0 -> "😿"
        else -> "🙀"
    }
}

