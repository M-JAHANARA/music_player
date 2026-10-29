package com.jahanmusic.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jahanmusic.app.ui.theme.*
import com.wexpa.liquidglass.GlassStyle
import com.wexpa.liquidglass.LiquidGlassState
import com.wexpa.liquidglass.liquidGlass

/**
 * Mini player above the footer: one tall glass card with art + title/artist +
 * prev/play/next and a slim progress line (shuffle/repeat icons removed).
 * Tap anywhere (except buttons) opens Playback.
 *
 * When [glass] is provided the card is drawn by liquidglass — real refraction
 * of the page content under the bar (tinted fallback below Android 13).
 */
@Composable
fun MusicControlBar(
    songTitle: String = "Blinding Lights",
    artistName: String = "The Weeknd",
    albumArt: Any? = null,
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onBarClick: () -> Unit = {},
    positionMs: Long = 0L,
    durationMs: Long = 0L,
    glass: LiquidGlassState? = null,
    modifier: Modifier = Modifier
) {
    val progress =
        if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (glass != null) {
                    // Real liquid glass (shayann07) over the recorded pages —
                    // dark, mostly opaque tint + frosted blur so the card reads
                    // as solid glass, not a clear window.
                    Modifier.liquidGlass(
                        state = glass,
                        shape = RoundedCornerShape(22.dp),
                        style = GlassStyle.Chrome.copy(
                            tint = Color(0xCC26263E),
                            backdropBlur = 24.dp,
                            fallbackSurface = Color(0xE61A1A2E)
                        )
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        // Glass card: translucent frosted surface (same family as the menu)
                        .background(Color(0xB31A1A2E))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x33B388FF),
                                    Color(0x1AE040FB),
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
                }
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x44FFFFFF), Color(0x12FFFFFF))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onBarClick() }
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (albumArt != null) {
                    AsyncImage(
                        model = albumArt,
                        contentDescription = null,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(AccentPurple, AccentMagenta))
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentPurple, AccentMagenta)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = songTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artistName,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentPurple, AccentMagenta)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Slim progress line filling the taller card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AccentPurple, AccentMagenta)
                            )
                        )
                )
            }
        }
    }
}
