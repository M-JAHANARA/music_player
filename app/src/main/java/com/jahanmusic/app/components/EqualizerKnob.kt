package com.jahanmusic.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.ui.theme.AccentMagenta
import com.jahanmusic.app.ui.theme.AccentPurple
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vendored equivalent of compose-audio-controls rotary knob.
 * Drag vertically (or circularly) to change 0..1 value.
 * Used for Bass + Virtualizer to match equalizer.jpg.
 */
@Composable
fun EqualizerKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    accent: Color,
    size: Dp = 100.dp,
    displayText: String = "${(value * 100).toInt()}%"
) {
    var angle by remember(value) { mutableFloatStateOf(-135f + value * 270f) }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Track
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.08f),
                            accent.copy(alpha = 0.35f),
                            accent.copy(alpha = 0.08f)
                        )
                    )
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        // vertical drag changes value
                    }
                }
                .pointerInput(value) {
                    // simple vertical drag detector
                },
            contentAlignment = Alignment.Center
        ) {
            // Inner dial
            Box(
                modifier = Modifier
                    .size(size * 0.72f)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }
        // Pointer indicator (rotating dot)
        val rad = Math.toRadians(angle.toDouble())
        val r = (size.value / 2f - 8f)
        // indicator drawn via offset box
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // vertical drag: up = +, down = -
                        val dv = (-dragAmount.y) / 300f
                        val nv = (value + dv).coerceIn(0f, 1f)
                        angle = -135f + nv * 270f
                        onValueChange(nv)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val dotX = (cos(rad) * r).dp
            val dotY = (sin(rad) * r).dp
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .pointerInput(Unit) { }
            )
        }
    }
}

/** Simple vertical slider matching equalizer.jpg columns. */
@Composable
fun EqVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accent: Color = AccentPurple,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = androidx.compose.material3.SliderDefaults.colors(
            thumbColor = AccentMagenta,
            activeTrackColor = accent,
            inactiveTrackColor = accent.copy(alpha = 0.2f)
        )
    )
}

private fun atan2deg(y: Float, x: Float): Float =
    (atan2(y, x) * 180.0 / Math.PI).toFloat()
