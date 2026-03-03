package com.taile.petevo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Emotion levels:
 *   >= 87 : Happy  (big smile, wagging tail, sparkling eyes)
 *   >= 50 : Neutral (small smile, normal eyes)
 *   >= 0  : Sad    (frown, droopy ears, teardrop)
 *   < 0   : Very Sad (big frown, tears, flattened ears)
 */
@Composable
@Preview
fun PetDogAvatar(
    emotion: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF4CAF50)
) {
    Canvas(modifier = modifier.size(140.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val furColor = Color(0xFFF5C77E)       // Golden fur
        val furDark = Color(0xFFD4A04A)         // Darker fur (ears, patches)
        val white = Color.White
        val black = Color(0xFF3E2723)           // Deep brown for outlines
        val nose = Color(0xFF4E342E)            // Nose color
        val tongue = Color(0xFFEF5350)          // Tongue / blush
        val cheek = Color(0xFFFFAB91)           // Cheek blush

        // ── Body (oval at bottom) ──
        drawOval(
            color = furColor,
            topLeft = Offset(cx - w * 0.25f, h * 0.62f),
            size = Size(w * 0.50f, h * 0.30f)
        )

        // ── Head (big circle) ──
        val headRadius = w * 0.30f
        val headCenter = Offset(cx, cy - h * 0.02f)
        drawCircle(color = furColor, radius = headRadius, center = headCenter)

        // ── Ears ──
        val earDroop = when {
            emotion >= 50 -> 0f       // ears up
            emotion >= 0 -> h * 0.04f // slightly droopy
            else -> h * 0.08f         // very droopy
        }
        // Left ear (floppy)
        val leftEar = Path().apply {
            moveTo(cx - headRadius * 0.65f, headCenter.y - headRadius * 0.55f)
            cubicTo(
                cx - headRadius * 1.35f, headCenter.y - headRadius * 1.1f + earDroop,
                cx - headRadius * 1.50f, headCenter.y - headRadius * 0.1f + earDroop,
                cx - headRadius * 0.75f, headCenter.y - headRadius * 0.05f + earDroop
            )
        }
        drawPath(leftEar, color = furDark, style = Fill)
        drawPath(leftEar, color = black, style = Stroke(width = 2f))

        // Right ear (floppy)
        val rightEar = Path().apply {
            moveTo(cx + headRadius * 0.65f, headCenter.y - headRadius * 0.55f)
            cubicTo(
                cx + headRadius * 1.35f, headCenter.y - headRadius * 1.1f + earDroop,
                cx + headRadius * 1.50f, headCenter.y - headRadius * 0.1f + earDroop,
                cx + headRadius * 0.75f, headCenter.y - headRadius * 0.05f + earDroop
            )
        }
        drawPath(rightEar, color = furDark, style = Fill)
        drawPath(rightEar, color = black, style = Stroke(width = 2f))

        // ── Re-draw head circle on top of ears ──
        drawCircle(color = furColor, radius = headRadius, center = headCenter)
        drawCircle(color = black, radius = headRadius, center = headCenter, style = Stroke(width = 2.5f))

        // ── Muzzle (lighter oval) ──
        val muzzleCenter = Offset(cx, headCenter.y + headRadius * 0.30f)
        drawOval(
            color = white,
            topLeft = Offset(muzzleCenter.x - headRadius * 0.45f, muzzleCenter.y - headRadius * 0.28f),
            size = Size(headRadius * 0.90f, headRadius * 0.55f)
        )

        // ── Eyes ──
        val eyeY = headCenter.y - headRadius * 0.10f
        val eyeSpacing = headRadius * 0.38f
        val leftEyeX = cx - eyeSpacing
        val rightEyeX = cx + eyeSpacing

        when {
            emotion >= 87 -> {
                // Happy sparkle eyes (closed happy arcs)
                drawHappyEye(leftEyeX, eyeY, headRadius * 0.13f, black)
                drawHappyEye(rightEyeX, eyeY, headRadius * 0.13f, black)
            }
            emotion >= 50 -> {
                // Normal eyes
                drawCircle(color = white, radius = headRadius * 0.15f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.09f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = white, radius = headRadius * 0.04f, center = Offset(leftEyeX - headRadius * 0.03f, eyeY - headRadius * 0.03f))

                drawCircle(color = white, radius = headRadius * 0.15f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.09f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = white, radius = headRadius * 0.04f, center = Offset(rightEyeX - headRadius * 0.03f, eyeY - headRadius * 0.03f))
            }
            emotion >= 0 -> {
                // Sad eyes (looking down)
                drawCircle(color = white, radius = headRadius * 0.14f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.08f, center = Offset(leftEyeX, eyeY + headRadius * 0.03f))

                drawCircle(color = white, radius = headRadius * 0.14f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.08f, center = Offset(rightEyeX, eyeY + headRadius * 0.03f))

                // Sad eyebrows
                drawSadEyebrow(leftEyeX, eyeY - headRadius * 0.20f, headRadius * 0.14f, black, isLeft = true)
                drawSadEyebrow(rightEyeX, eyeY - headRadius * 0.20f, headRadius * 0.14f, black, isLeft = false)
            }
            else -> {
                // Very sad eyes (teary)
                drawCircle(color = white, radius = headRadius * 0.14f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.07f, center = Offset(leftEyeX, eyeY + headRadius * 0.04f))

                drawCircle(color = white, radius = headRadius * 0.14f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = black, radius = headRadius * 0.07f, center = Offset(rightEyeX, eyeY + headRadius * 0.04f))

                // Sad eyebrows
                drawSadEyebrow(leftEyeX, eyeY - headRadius * 0.20f, headRadius * 0.14f, black, isLeft = true)
                drawSadEyebrow(rightEyeX, eyeY - headRadius * 0.20f, headRadius * 0.14f, black, isLeft = false)

                // Teardrops
                drawTeardrop(leftEyeX - headRadius * 0.10f, eyeY + headRadius * 0.20f, headRadius * 0.06f, Color(0xFF64B5F6))
                drawTeardrop(rightEyeX + headRadius * 0.10f, eyeY + headRadius * 0.20f, headRadius * 0.06f, Color(0xFF64B5F6))
            }
        }

        // ── Nose ──
        val noseCenter = Offset(cx, muzzleCenter.y - headRadius * 0.05f)
        val nosePath = Path().apply {
            moveTo(noseCenter.x, noseCenter.y - headRadius * 0.06f)
            lineTo(noseCenter.x - headRadius * 0.08f, noseCenter.y + headRadius * 0.05f)
            lineTo(noseCenter.x + headRadius * 0.08f, noseCenter.y + headRadius * 0.05f)
            close()
        }
        drawPath(nosePath, color = nose, style = Fill)
        // Nose shine
        drawCircle(color = Color.White.copy(alpha = 0.3f), radius = headRadius * 0.025f,
            center = Offset(noseCenter.x - headRadius * 0.02f, noseCenter.y - headRadius * 0.02f))

        // ── Mouth ──
        val mouthY = noseCenter.y + headRadius * 0.10f
        when {
            emotion >= 87 -> {
                // Big happy smile with tongue
                val smilePath = Path().apply {
                    moveTo(cx - headRadius * 0.25f, mouthY)
                    cubicTo(
                        cx - headRadius * 0.15f, mouthY + headRadius * 0.22f,
                        cx + headRadius * 0.15f, mouthY + headRadius * 0.22f,
                        cx + headRadius * 0.25f, mouthY
                    )
                }
                drawPath(smilePath, color = black, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                // Tongue sticking out
                drawOval(
                    color = tongue,
                    topLeft = Offset(cx - headRadius * 0.08f, mouthY + headRadius * 0.06f),
                    size = Size(headRadius * 0.16f, headRadius * 0.18f)
                )
            }
            emotion >= 50 -> {
                // Small smile
                val smilePath = Path().apply {
                    moveTo(cx - headRadius * 0.15f, mouthY)
                    cubicTo(
                        cx - headRadius * 0.08f, mouthY + headRadius * 0.12f,
                        cx + headRadius * 0.08f, mouthY + headRadius * 0.12f,
                        cx + headRadius * 0.15f, mouthY
                    )
                }
                drawPath(smilePath, color = black, style = Stroke(width = 2f, cap = StrokeCap.Round))
            }
            emotion >= 0 -> {
                // Slight frown
                val frownPath = Path().apply {
                    moveTo(cx - headRadius * 0.15f, mouthY + headRadius * 0.08f)
                    cubicTo(
                        cx - headRadius * 0.08f, mouthY - headRadius * 0.02f,
                        cx + headRadius * 0.08f, mouthY - headRadius * 0.02f,
                        cx + headRadius * 0.15f, mouthY + headRadius * 0.08f
                    )
                }
                drawPath(frownPath, color = black, style = Stroke(width = 2f, cap = StrokeCap.Round))
            }
            else -> {
                // Big frown / wobble mouth
                val frownPath = Path().apply {
                    moveTo(cx - headRadius * 0.20f, mouthY + headRadius * 0.12f)
                    cubicTo(
                        cx - headRadius * 0.10f, mouthY - headRadius * 0.05f,
                        cx + headRadius * 0.10f, mouthY - headRadius * 0.05f,
                        cx + headRadius * 0.20f, mouthY + headRadius * 0.12f
                    )
                }
                drawPath(frownPath, color = black, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }
        }

        // ── Cheek blush (for happy states) ──
        if (emotion >= 50) {
            drawCircle(color = cheek.copy(alpha = 0.4f), radius = headRadius * 0.10f,
                center = Offset(leftEyeX - headRadius * 0.12f, eyeY + headRadius * 0.22f))
            drawCircle(color = cheek.copy(alpha = 0.4f), radius = headRadius * 0.10f,
                center = Offset(rightEyeX + headRadius * 0.12f, eyeY + headRadius * 0.22f))
        }


        // ── Front paws ──
        val pawY = h * 0.85f
        val pawRadius = w * 0.06f
        // Left paw
        drawOval(
            color = furColor,
            topLeft = Offset(cx - w * 0.18f, pawY),
            size = Size(pawRadius * 2f, pawRadius * 1.4f)
        )
        drawOval(
            color = furColor,
            topLeft = Offset(cx + w * 0.06f, pawY),
            size = Size(pawRadius * 2f, pawRadius * 1.4f)
        )
        // Paw pads
        drawCircle(color = furDark, radius = pawRadius * 0.35f, center = Offset(cx - w * 0.12f, pawY + pawRadius * 0.9f))
        drawCircle(color = furDark, radius = pawRadius * 0.35f, center = Offset(cx + w * 0.12f, pawY + pawRadius * 0.9f))

        // ── Collar ──
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(cx - headRadius * 0.50f, headCenter.y + headRadius * 0.82f),
            size = Size(headRadius * 1.0f, headRadius * 0.14f),
            cornerRadius = CornerRadius(headRadius * 0.07f)
        )
        // Collar tag
        drawCircle(color = Color(0xFFFFD54F), radius = headRadius * 0.07f,
            center = Offset(cx, headCenter.y + headRadius * 0.96f))
    }
}

// ── Helper drawing functions ──

private fun DrawScope.drawHappyEye(x: Float, y: Float, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(x - radius, y)
        cubicTo(
            x - radius * 0.5f, y - radius * 1.2f,
            x + radius * 0.5f, y - radius * 1.2f,
            x + radius, y
        )
    }
    drawPath(path, color = color, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSadEyebrow(x: Float, y: Float, length: Float, color: Color, isLeft: Boolean) {
    val path = Path().apply {
        if (isLeft) {
            moveTo(x - length, y + length * 0.3f)
            lineTo(x + length * 0.3f, y - length * 0.1f)
        } else {
            moveTo(x - length * 0.3f, y - length * 0.1f)
            lineTo(x + length, y + length * 0.3f)
        }
    }
    drawPath(path, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawTeardrop(x: Float, y: Float, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y - radius * 1.5f)
        cubicTo(
            x + radius * 0.8f, y - radius * 0.5f,
            x + radius, y + radius * 0.5f,
            x, y + radius
        )
        cubicTo(
            x - radius, y + radius * 0.5f,
            x - radius * 0.8f, y - radius * 0.5f,
            x, y - radius * 1.5f
        )
    }
    drawPath(path, color = color, style = Fill)
}

