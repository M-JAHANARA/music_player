package com.jahanmusic.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jahanmusic.app.R
import com.jahanmusic.app.player.RepeatMode
import com.jahanmusic.app.ui.theme.AccentMagenta
import com.jahanmusic.app.ui.theme.AccentPurple
import com.jahanmusic.app.ui.theme.AppBackground
import com.jahanmusic.app.viewmodel.MusicViewModel

/**
 * Playback recreated to match play_screen.jpg:
 * - blurred album art fills the whole background (dark scrim over it)
 * - top bar: back arrow FIRST at the left, then title + artist
 * - big circular album art (no tick ring, no lines around it)
 * - like / tik / alarm / more row, purple slider + times
 * - navigation row like the image: shuffle | prev | BIG ringed play/pause
 *   | next | repeat(4 modes)
 * - every control is glass (translucent + hairline border), pink/purple
 *   accents identical to the mini player card.
 */
@Composable
fun PlaybackScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    vm: MusicViewModel = viewModel()
) {
    val song by vm.currentSong.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val pos by vm.positionMs.collectAsState()
    val dur by vm.durationMs.collectAsState()
    val repeatMode by vm.repeatMode.collectAsState()

    var liked by remember { mutableStateOf(false) }
    var tikMarked by remember { mutableStateOf(false) }

    val progress = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
    val total = if (dur > 0) dur else song?.duration ?: 0L
    val artUri = song?.albumArtUri

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Chosen appearance background (whole app theme)
        AppBackground(LocalContext.current)
        // Blurred album art as the whole background (like the reference)
        if (artUri != null) {
            AsyncImage(
                model = artUri,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .scale(1.3f)
                    .blur(70.dp),
                contentScale = ContentScale.Crop
            )
        }
        // Scrim so text/icons stay readable over any art
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x59000000), Color(0x99000000), Color(0xCC000000))
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // ── Top bar: back arrow FIRST (left), then the song name ─────
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Column {
                    Text(
                        text = song?.title ?: "آهنگ در حال پخش",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song?.artist ?: "",
                        fontSize = 13.sp,
                        color = Color(0xB3FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.9f))

            // ── Big circular album art (no ring lines around it) ─────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2A3A34), Color(0xFF16211E)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (artUri != null) {
                    AsyncImage(
                        model = artUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote, "Album Art",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(110.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // ── like / tik / alarm / more (all glass) ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconCircle(size = 46.dp, onClick = { liked = !liked }) {
                    Icon(
                        if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Like",
                        tint = if (liked) Color(0xFFFF4D6D) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                GlassIconCircle(size = 46.dp, onClick = { tikMarked = !tikMarked }) {
                    Icon(
                        Icons.Outlined.Repeat,
                        "Tik",
                        tint = if (tikMarked) AccentMagenta else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                GlassIconCircle(size = 46.dp, onClick = { }) {
                    Icon(Icons.Outlined.Alarm, "Alarm", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                // "More" opens the equalizer (top-right icons were removed)
                GlassIconCircle(size = 46.dp, onClick = onNavigateToEqualizer) {
                    Icon(Icons.Default.MoreVert, "More", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── slider + times (pink/purple like the player card) ────────
            Slider(
                value = progress,
                onValueChange = { v -> vm.seekTo((v * total).toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentPurple,
                    activeTrackColor = AccentMagenta,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(vm.formatDuration(pos), fontSize = 12.sp, color = Color(0xB3FFFFFF))
                Text(vm.formatDuration(total), fontSize = 12.sp, color = Color(0xB3FFFFFF))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── navigation row, like the image ───────────────────────────
            // shuffle | prev | BIG ringed play/pause | next | repeat(4 modes)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconCircle(size = 50.dp, onClick = { vm.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle, "Shuffle",
                        tint = if (repeatMode == RepeatMode.SHUFFLE_ALL) AccentMagenta else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                GlassIconCircle(size = 54.dp, onClick = { vm.previous() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                // Big play/pause: pink→purple gradient ring, glass inside
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, AccentMagenta))
                        )
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33FFFFFF), Color(0x14FFFFFF))
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(listOf(Color(0x44FFFFFF), Color(0x14FFFFFF))),
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { vm.togglePlayPause() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                GlassIconCircle(size = 54.dp, onClick = { vm.next() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                // 4-mode repeat: order → loop all → shuffle all → repeat one
                GlassIconCircle(size = 50.dp, onClick = { vm.cycleRepeatMode() }) {
                    when (repeatMode) {
                        RepeatMode.ORDER -> Icon(
                            Icons.Outlined.Repeat, "Order",
                            tint = Color(0xB3FFFFFF), modifier = Modifier.size(26.dp)
                        )
                        RepeatMode.LOOP_ALL -> Icon(
                            Icons.Default.Repeat, "Loop all",
                            tint = AccentPurple, modifier = Modifier.size(26.dp)
                        )
                        RepeatMode.SHUFFLE_ALL -> Icon(
                            Icons.Default.Shuffle, "Shuffle all",
                            tint = AccentMagenta, modifier = Modifier.size(26.dp)
                        )
                        RepeatMode.REPEAT_ONE -> Icon(
                            Icons.Default.RepeatOne, "Repeat current",
                            tint = AccentPurple, modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.navigationBarsPadding().height(6.dp))
        }
    }
}

/** Glass control: translucent circle + hairline border, ripple-free tap. */
@Composable
private fun GlassIconCircle(
    size: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(listOf(Color(0x26FFFFFF), Color(0x0FFFFFFF)))
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x14FFFFFF))),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) { content() }
}
