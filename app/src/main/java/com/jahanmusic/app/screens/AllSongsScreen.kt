package com.jahanmusic.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jahanmusic.app.components.GlassmorphismNavigation
import com.jahanmusic.app.components.MusicControlBar
import com.jahanmusic.app.components.glassCard
import com.jahanmusic.app.data.DeviceSong
import com.jahanmusic.app.ui.theme.*
import com.jahanmusic.app.viewmodel.MusicViewModel

/**
 * All songs matches all_music.jpg: back + title (no top-right search icon,
 * search lives in the search bar). Same mini-player distance as Home.
 */
@Composable
fun AllSongsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayback: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAlbums: () -> Unit = {},
    vm: MusicViewModel = viewModel(),
    showFooter: Boolean = true,
    // Height of the floating player/menu chrome + 5dp: the list viewport
    // ends here, so only songs are visible — nothing behind the playback card.
    chromeHeight: Dp = 310.dp
) {
    var selectedNavIndex by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.checkPermission() }

    val songs by vm.songs.collectAsState()
    val currentSong by vm.currentSong.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    val filteredSongs = remember(searchQuery, songs) {
        if (searchQuery.isEmpty()) songs
        else songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            bottomBar = {
                if (showFooter) {
                Column {
                    MusicControlBar(
                        songTitle = currentSong?.title ?: "Blinding Lights",
                        artistName = currentSong?.artist ?: "The Weeknd",
                        albumArt = currentSong?.albumArtUri,
                        isPlaying = isPlaying,
                        onPlayPause = { vm.togglePlayPause() },
                        onNext = { vm.next() },
                        onPrevious = { vm.previous() },
                        onBarClick = onNavigateToPlayback,
                    )
                    GlassmorphismNavigation(
                        selectedIndex = selectedNavIndex,
                        onItemSelected = { index ->
                            selectedNavIndex = index
                            when (index) {
                                0 -> onNavigateToHome()
                                1 -> {}
                                2 -> onNavigateToAlbums()
                                3 -> onNavigateToSettings()
                            }
                        }
                    )
                }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header: back arrow FIRST at the top-left, then the page name
                // (Row forced LTR so the arrow is on the left even in RTL pages)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "همه آهنگ‌ها",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                }
                // Search bar right under the header (order: back+title, then search)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("جستجو در آهنگ‌ها...", color = TextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search", tint = TextSecondary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = Color(0x33B388FF),
                        // Glass: translucent so the page shows through
                        focusedContainerColor = Color(0xCC1A1A2E),
                        unfocusedContainerColor = Color(0x991A1A2E),
                        cursorColor = AccentPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                // Exactly 5dp from the search card to the first song card
                Spacer(modifier = Modifier.height(5.dp))
                if (filteredSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .padding(bottom = if (showFooter) 0.dp else chromeHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MusicNote, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("آهنگی یافت نشد", fontSize = 16.sp, color = TextSecondary)
                            Text("دسترسی به موسیقی را فعال کنید", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(
                        // The viewport ends exactly at the floating playback
                        // card (chromeHeight already includes its +5dp gap):
                        // only songs are visible here, no content can appear
                        // behind the playback / control card. Top padding is
                        // 0 so the first card sits exactly 5dp under search.
                        modifier = Modifier.weight(1f)
                            .padding(bottom = if (showFooter) 0.dp else chromeHeight),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredSongs, key = { _, s -> s.id }) { index, song ->
                            DeviceSongListItem(
                                song = song,
                                onClick = {
                                    vm.playAt(songs.indexOf(song).takeIf { it >= 0 } ?: index)
                                    onNavigateToPlayback()
                                }
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun DeviceSongListItem(song: DeviceSong, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .glassCard(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier.size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(AccentPurple, AccentMagenta))),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(formatDuration(song.duration), fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.MoreVert, "More", tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
