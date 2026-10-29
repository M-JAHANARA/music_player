package com.jahanmusic.app.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jahanmusic.app.components.MusicControlBar
import com.jahanmusic.app.components.SwipeableGlassNav
import com.jahanmusic.app.ui.theme.AppBackground
import com.jahanmusic.app.ui.theme.DarkBackground
import com.jahanmusic.app.viewmodel.MusicViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wexpa.liquidglass.liquidGlassSource
import com.wexpa.liquidglass.rememberLiquidGlassState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * The Settings tab hides the mini player, so only the glass menu floats
 * there (141dp) — its cards stop just above the menu.
 */
internal val NavOnlyHeight = 141.dp
internal val SettingsBottomInset = NavOnlyHeight + 12.dp

/**
 * Swipeable main tabs (tap + drag):
 * 0 Home | 1 Songs | 2 Albums | 3 Settings.
 *
 * Pages fill the whole screen and the chrome (mini player + glass menu) floats
 * over them, so the glass refracts REAL page content underneath:
 * - Kyant0 Backdrop ([rememberLayerBackdrop]/[layerBackdrop]) feeds the menu.
 * - liquidglass ([rememberLiquidGlassState]/[liquidGlassSource]) feeds the
 *   mini player.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(
    initialPage: Int = 0,
    onNavigateToPlayback: () -> Unit,
    onNavigateToAppearance: () -> Unit = {},
    vm: MusicViewModel = viewModel()
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, 3),
        pageCount = { 4 }
    )
    val scope = rememberCoroutineScope()

    // Real height of the floating chrome (mini player + glass menu), measured
    // at runtime so the Songs/Albums lists can stop EXACTLY 5dp above the
    // playback card — no content ever shows behind it.
    var chromeHeightPx by remember { mutableStateOf(0) }
    val chromeHeight = with(LocalDensity.current) { chromeHeightPx.toDp() }
    val listBottomInset =
        if (chromeHeightPx > 0) chromeHeight + 5.dp else 310.dp

    val currentSong by vm.currentSong.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val positionMs by vm.positionMs.collectAsState()
    val durationMs by vm.durationMs.collectAsState()

    fun goTo(page: Int) {
        // Gentle HyperOS glide instead of an instant snap
        scope.launch {
            pagerState.animateScrollToPage(
                page.coerceIn(0, 3),
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Pager kept LTR so swipe-left = next page on every device
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            // Backdrop recorded from the page content (drawn with the ground)
            val pageBackdrop = rememberLayerBackdrop {
                drawRect(DarkBackground)
                drawContent()
            }
            // Separate liquidglass backdrop for the mini player
            val glass = rememberLiquidGlassState(background = DarkBackground)

            // Wrapper keeps the chosen wallpaper INSIDE both glass backdrops, so
            // the floating chrome refracts the real themed page (background+blur).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(pageBackdrop)
                    .liquidGlassSource(glass)
            ) {
                // Live appearance background (bg1..bg8 / gallery pick + blur)
                AppBackground(LocalContext.current)

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    // Soft page transition: the incoming page fades up + settles
                    // from a hair smaller; the outgoing page dims slightly.
                    // Smooths both swipes and tab-jumps.
                    // Signed distance of this page from the viewport centre
                    // (same formula the old calculateCurrentOffsetForPage used).
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val distance = pageOffset.absoluteValue.coerceIn(0f, 1f)
                    val anim by animateFloatAsState(
                        targetValue = distance,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "pageTransition"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = 1f - 0.55f * anim
                                scaleX = 1f - 0.06f * anim
                                scaleY = 1f - 0.06f * anim
                            }
                    ) {
                        when (page) {
                            0 -> HomeScreen(
                            onNavigateToAllSongs = { goTo(1) },
                            onNavigateToAlbums = { goTo(2) },
                            onNavigateToSettings = { goTo(3) },
                            onNavigateToPlayback = onNavigateToPlayback,
                            vm = vm,
                            showFooter = false
                        )
                        1 -> AllSongsScreen(
                            onNavigateBack = { goTo(0) },
                            onNavigateToPlayback = onNavigateToPlayback,
                            onNavigateToSettings = { goTo(3) },
                            onNavigateToHome = { goTo(0) },
                            onNavigateToAlbums = { goTo(2) },
                            vm = vm,
                            showFooter = false,
                            chromeHeight = listBottomInset
                        )
                        2 -> AlbumsScreen(
                            onNavigateBack = { goTo(0) },
                            onNavigateToHome = { goTo(0) },
                            onNavigateToAllSongs = { goTo(1) },
                            onNavigateToSettings = { goTo(3) },
                            vm = vm,
                            showFooter = false,
                            chromeHeight = listBottomInset
                        )
                        else -> SettingsScreen(
                            onNavigateBack = { goTo(0) },
                            onNavigateToHome = { goTo(0) },
                            onNavigateToAllSongs = { goTo(1) },
                            onNavigateToAlbums = { goTo(2) },
                            onNavigateToAppearance = onNavigateToAppearance,
                            showFooter = false
                        )
                        }
                    }
                }
            }

            // Floating chrome over the content (real refraction underneath).
            // Settings tab: NO mini player card — only the glass menu floats.
            // Measured so the lists know where the playback card starts.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { chromeHeightPx = it.height }
            ) {
                if (pagerState.currentPage != 3) {
                    MusicControlBar(
                        songTitle = currentSong?.title ?: "Blinding Lights",
                        artistName = currentSong?.artist ?: "The Weeknd",
                        albumArt = currentSong?.albumArtUri,
                        isPlaying = isPlaying,
                        onPlayPause = { vm.togglePlayPause() },
                        onNext = { vm.next() },
                        onPrevious = { vm.previous() },
                        onBarClick = onNavigateToPlayback,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        glass = glass
                    )
                }
                SwipeableGlassNav(
                    selectedIndex = pagerState.currentPage,
                    pagerPage = pagerState.currentPage,
                    pagerOffsetFraction = try {
                        pagerState.currentPageOffsetFraction
                    } catch (_: Exception) { 0f },
                    onTabSelected = { goTo(it) },
                    backdrop = pageBackdrop
                )
            }
        }
    }
}
