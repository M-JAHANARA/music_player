package com.jahanmusic.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
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
import com.jahanmusic.app.ui.theme.*
import com.jahanmusic.app.viewmodel.MusicViewModel

@Composable
fun AlbumsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAllSongs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    vm: MusicViewModel = viewModel(),
    showFooter: Boolean = true,
    // Height of the floating player/menu chrome + 5dp: the grid viewport
    // ends here, so only albums are visible — nothing behind the playback card.
    chromeHeight: Dp = 310.dp
) {
    var selectedNavIndex by remember { mutableIntStateOf(2) }
    LaunchedEffect(Unit) { vm.checkPermission() }
    val albums by vm.albums.collectAsState()
    val currentSong by vm.currentSong.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

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
                        onBarClick = {},
                    )
                    GlassmorphismNavigation(
                        selectedIndex = selectedNavIndex,
                        onItemSelected = { index ->
                            selectedNavIndex = index
                            when (index) {
                                0 -> onNavigateToHome()
                                1 -> onNavigateToAllSongs()
                                2 -> {}
                                3 -> onNavigateToSettings()
                            }
                        }
                    )
                }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Header: back arrow FIRST at the top-left, then the page name
                // (Row forced LTR so the arrow is on the left even in RTL pages)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text("آلبوم‌ها", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
                }
                }
                if (albums.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().weight(1f)
                            .padding(bottom = if (showFooter) 0.dp else chromeHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("آلبومی یافت نشد", color = TextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        // The viewport ends exactly at the floating playback
                        // card (chromeHeight already includes its +5dp gap):
                        // only albums are visible here, no content can appear
                        // behind the playback / control card.
                        modifier = Modifier.weight(1f)
                            .padding(bottom = if (showFooter) 0.dp else chromeHeight),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(albums) { a ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .glassCard(RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(AccentPurple, AccentMagenta))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Album, a.name, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(a.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${a.artist} • ${a.songCount} آهنگ", fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
