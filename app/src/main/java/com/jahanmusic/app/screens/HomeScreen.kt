package com.jahanmusic.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jahanmusic.app.R
import com.jahanmusic.app.components.GlassmorphismNavigation
import com.jahanmusic.app.components.MusicControlBar
import com.jahanmusic.app.ui.theme.*
import com.jahanmusic.app.viewmodel.MusicViewModel

/**
 * Home matches home.jpg (modified per spec):
 * Top = crown (left) + "Jahan Music" (right), no search/settings icons.
 * Row1: all songs | playlists | albums. Row2: wide favorites.
 * No suggestions section. Mini player + glass footer.
 */
@Composable
fun HomeScreen(
    onNavigateToAllSongs: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayback: () -> Unit,
    vm: MusicViewModel = viewModel(),
    showFooter: Boolean = true
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { vm.checkPermission() }

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
                        onBarClick = onNavigateToPlayback,
                    )
                    GlassmorphismNavigation(
                        selectedIndex = selectedNavIndex,
                        onItemSelected = { index ->
                            selectedNavIndex = index
                            when (index) {
                                0 -> {}
                                1 -> onNavigateToAllSongs()
                                2 -> onNavigateToAlbums()
                                3 -> onNavigateToSettings()
                            }
                        }
                    )
                }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar (RTL row): first child = right → crown right, name left
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = "Premium",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Jahan Music",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                // Cards left-to-right: songs, playlists, albums
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val categories = listOf(
                            CategoryItem("همه آهنگ‌ها", Icons.Default.MusicNote, listOf(AccentPurple, AccentMagenta)),
                            CategoryItem("پلی‌لیست‌ها", Icons.AutoMirrored.Filled.QueueMusic, listOf(AccentMagenta, AccentPink)),
                            CategoryItem("آلبوم‌ها", Icons.Default.Album, listOf(AccentPink, AccentPurple))
                        )
                        categories.forEach { category ->
                            CategoryCard(
                                category = category,
                                onClick = {
                                    when (category.title) {
                                        "همه آهنگ‌ها" -> onNavigateToAllSongs()
                                        "آلبوم‌ها" -> onNavigateToAlbums()
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                CategoryCard(
                    category = CategoryItem(
                        "علاقه‌مندی‌ها", Icons.Default.Favorite,
                        listOf(AccentPurple.copy(alpha = 0.8f), AccentMagenta.copy(alpha = 0.8f)),
                        isWide = true
                    ),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Bottom gap = menu card height so favorites sit one menu-height above footer
                Spacer(modifier = Modifier.height(68.dp))
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .then(if (category.isWide) Modifier.height(100.dp) else Modifier.aspectRatio(1f))
            .clip(RoundedCornerShape(20.dp))
            // Glass card: translucent tinted surface instead of solid paint
            .background(Color(0x5916162A))
            .background(
                Brush.linearGradient(
                    colors = category.gradientColors.map { it.copy(alpha = 0.22f) }
                )
            )
            // Specular top-edge sheen (liquid glass light catch)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                    startY = 0f,
                    endY = 90f
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x55FFFFFF), Color(0x14FFFFFF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = Color.White,
                modifier = Modifier.size(if (category.isWide) 28.dp else 36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                fontSize = if (category.isWide) 14.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class CategoryItem(
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val isWide: Boolean = false
)
