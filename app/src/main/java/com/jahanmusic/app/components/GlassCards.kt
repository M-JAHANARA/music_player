package com.jahanmusic.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Frosted-glass card surface matching glassmorphism.jpg: translucent dark
 * base + purple/magenta sheen + top-edge rim light + 1dp gradient border.
 * Same recipe as the Home category cards, reusable on any page.
 */
fun Modifier.glassCard(
    cornerRadius: RoundedCornerShape = RoundedCornerShape(16.dp)
): Modifier = this
    .shadow(12.dp, cornerRadius)
    .clip(cornerRadius)
    // Translucent dark base — the page shows through
    .background(Color(0x991A1A2E))
    // Purple / magenta sheen
    .background(
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x33B388FF),
                Color(0x12E040FB),
                Color(0x33B388FF)
            )
        )
    )
    // Top-edge rim light
    .background(
        Brush.verticalGradient(
            colors = listOf(Color(0x44FFFFFF), Color.Transparent),
            startY = 0f,
            endY = 60f
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x44FFFFFF), Color(0x12FFFFFF))
        ),
        shape = cornerRadius
    )
