package com.jahanmusic.app.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.ui.theme.*
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * HyperOS / Miuix-style swipeable glass footer.
 *
 * - Order left-to-right: home, songs, albums, settings.
 * - The selector is a WATER DROP: it appears when a tab is tapped, and it can
 *   be grabbed and dragged across the bar — it stretches like water while
 *   moving and lands on the tab under the finger on release.
 * - Dragging the pager also drives the selector live between tabs.
 * - HyperOS physics: low-stiffness spring on the traveling selector.
 * - Liquid press lens: the tab under the finger scales up with a specular
 *   highlight, like a lens stretching 1:1 with the touch.
 *
 * The bar itself refracts real page content via Kyant0 Backdrop
 * ([backdrop] is passed in from MainTabsScreen).
 */
@Composable
fun SwipeableGlassNav(
    selectedIndex: Int,
    pagerPage: Int = selectedIndex,
    pagerOffsetFraction: Float = 0f,
    onTabSelected: (Int) -> Unit,
    backdrop: LayerBackdrop? = null,
    modifier: Modifier = Modifier
) {
    // LTR: home left … settings right
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                // Bottom gap: the pill floats well above the system navigation
                // row (raised to match the airier spacing of glassmorphism.jpg).
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 76.dp)
        ) {
            val tabCount = navItems.size
            val density = LocalDensity.current

            // Tab cells live inside 8dp inner margins → use the true cell width
            val tabWidth = (maxWidth - 16.dp) / tabCount
            val tabWidthPx = with(density) { tabWidth.toPx() }
            // Capsule x origin: 8dp cell margin + 2dp centering within the cell
            val capsuleBasePx = with(density) { 10.dp.toPx() }

            // Selector follows the pager …
            val pagerProgress = (pagerPage + pagerOffsetFraction)
                .coerceIn(0f, (tabCount - 1).toFloat())
            // … unless the water drop is being dragged by hand on the bar.
            var dragProgress by remember { mutableStateOf<Float?>(null) }
            val rawProgress = dragProgress ?: pagerProgress
            // HyperOS spring: smooth, no-bounce glide — the drop slides
            // gracefully to the tapped tab (or under the dragging finger).
            val progress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = 210f
                ),
                label = "selector"
            )
            // Water stretch: the drop elongates while it is traveling
            // (chasing the finger or springing onto a tab) and relaxes
            // back to a round pill when it settles.
            val stretch by animateFloatAsState(
                targetValue = 1f +
                        ((rawProgress - progress).absoluteValue * 0.22f)
                            .coerceAtMost(0.5f),
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 260f
                ),
                label = "dropStretch"
            )
            var pressedIndex by remember { mutableIntStateOf(-1) }

            // The drop only exists during interaction: it appears on press /
            // page change, travels, then evaporates shortly after landing.
            var dropVisible by remember { mutableStateOf(false) }
            var fingerDown by remember { mutableStateOf(false) }
            val dropAlpha by animateFloatAsState(
                targetValue = if (dropVisible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = if (dropVisible) 140 else 420,
                    easing = FastOutSlowInEasing
                ),
                label = "dropAlpha"
            )
            // Evaporate a moment after the finger lifts (drop has landed).
            LaunchedEffect(dropVisible, fingerDown) {
                if (dropVisible && !fingerDown) {
                    delay(650)
                    if (!fingerDown) dropVisible = false
                }
            }
            // Page swipes (finger on the page, not the bar) also summon the
            // drop onto the newly selected tab.
            var firstPageRender by remember { mutableStateOf(true) }
            LaunchedEffect(pagerPage) {
                if (firstPageRender) {
                    firstPageRender = false
                } else {
                    dropVisible = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Grab & drag the water drop anywhere on the bar:
                    // horizontal drag moves the drop between tabs; lifting
                    // lands it on the tab under the finger.
                    .pointerInput(tabCount) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            fingerDown = true
                            dropVisible = true
                            val downX = down.position.x
                            val downY = down.position.y
                            var dragging = false
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.isEmpty() ||
                                    event.changes.none { it.pressed }
                                ) break
                                val p = event.changes.firstOrNull { it.pressed } ?: break
                                val dx = p.position.x - downX
                                val dy = p.position.y - downY
                                if (!dragging &&
                                    abs(dx) > viewConfiguration.touchSlop &&
                                    abs(dx) > abs(dy)
                                ) {
                                    dragging = true
                                }
                                if (dragging) {
                                    dragProgress =
                                        ((p.position.x - capsuleBasePx) / tabWidthPx)
                                            .coerceIn(0f, (tabCount - 1).toFloat())
                                }
                            }
                            if (dragging) {
                                val target =
                                    (dragProgress ?: 0f).roundToInt().coerceIn(0, tabCount - 1)
                                onTabSelected(target)
                            }
                            dragProgress = null
                            fingerDown = false
                        }
                    }
            ) {
                // The bar records ITSELF into this backdrop, so the water drop
                // can magnify the MENU (its glass + icons) — not the page
                // content behind the bar.
                val barBackdrop = rememberLayerBackdrop { drawContent() }

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .layerBackdrop(barBackdrop)
                        .shadow(20.dp, RoundedCornerShape(29.dp))
                        .then(
                            if (backdrop != null) {
                                // Real backdrop refraction (Kyant0 Backdrop):
                                // the bar bends the page pixels underneath.
                                // blur works on Android 12+, lens needs Android 13+.
                                Modifier.drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { RoundedCornerShape(29.dp) },
                                    effects = {
                                        vibrancy()
                                        // Softer frost = smoother glass
                                        blur(14f.dp.toPx())
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            lens(12f.dp.toPx(), 24f.dp.toPx())
                                        }
                                    },
                                    onDrawSurface = {
                                        drawRect(Color(0x991A1A2E))
                                        drawRect(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0x40B388FF),
                                                    Color(0x20E040FB),
                                                    Color(0x40B388FF)
                                                )
                                            )
                                        )
                                    }
                                )
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(29.dp))
                                    // Frosted dark glass bar (glassmorphism.jpg)
                                    .background(Color(0xB31A1A2E))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x40B388FF),
                                                Color(0x20E040FB),
                                                Color(0x40B388FF)
                                            )
                                        )
                                    )
                                    // Top-edge light catch for the liquid rim
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x55FFFFFF),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = 60f
                                        )
                                    )
                            }
                        )
                        .clip(RoundedCornerShape(29.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(29.dp)
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        val isPressed = index == pressedIndex
                        // Lens under the finger: pressed tab pops 1:1 with touch.
                        val lens by animateFloatAsState(
                            targetValue = if (isPressed) 1.18f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "lens"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .scale(lens)
                                .clip(RoundedCornerShape(20.dp))
                                // No white wash under the pressed tab — the water
                                // drop is the tap feedback (see glassmorphism.jpg).
                                .pointerInput(index) {
                                    detectTapGestures(
                                        onPress = {
                                            pressedIndex = index
                                            // Select IMMEDIATELY on touch-down so
                                            // the drop starts sliding with no pause.
                                            onTabSelected(index)
                                            tryAwaitRelease()
                                            pressedIndex = -1
                                        }
                                    )
                                }
                                // NOTE: no white press highlight here — the drop
                                // itself is the only tap feedback (glassmorphism.jpg).
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected || isPressed) Color.White else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    color = if (isSelected || isPressed) Color.White else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // The water drop selector — drawn ON TOP of the bar card so
                // it reads as a drop of water sitting on the glass. Only
                // visible during interaction (tap / hold / drag / page swipe);
                // evaporates shortly after it lands.
                if (dropAlpha > 0.004f) {
                    // A real drop of water is WIDER than the tab it sits on —
                    // it bulges over its neighbours (BakaSU-style).
                    val dropExtra = 12.dp
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset {
                                IntOffset(
                                    x = (capsuleBasePx + progress * tabWidthPx -
                                            with(density) { (dropExtra / 2).toPx() }).toInt(),
                                    y = 0
                                )
                            }
                            .width(tabWidth + dropExtra)
                            .height(70.dp)
                            .graphicsLayer {
                                scaleX = stretch
                                scaleY = 1f / sqrt(stretch)
                                alpha = dropAlpha
                            }
                            .shadow(16.dp, RoundedCornerShape(35.dp))
                            .then(
                                // The drop magnifies the MENU BAR itself
                                // (barBackdrop), exactly like the reference:
                                // through the drop you see the bar's glass and
                                // icons bent by the lens — never the page.
                                Modifier.drawBackdrop(
                                    backdrop = barBackdrop,
                                    shape = { RoundedCornerShape(35.dp) },
                                    effects = {
                                        vibrancy()
                                        blur(0.6f.dp.toPx())
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            lens(22f.dp.toPx(), 40f.dp.toPx())
                                        }
                                    },
                                    onDrawSurface = {
                                        // Just a breath of light — no milk
                                        drawRect(Color(0x08FFFFFF))
                                    }
                                )
                            )
                            .clip(RoundedCornerShape(35.dp))
                            // NO milky sheen fill — only a thin light catch on
                            // the top edge, like the photo.
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x14FFFFFF),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = 26f
                                )
                            )
                            // Shadow at the base of the bulge (water thickness)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x14000000)
                                    )
                                )
                            )
                            // Crisp liquid rim — bright, thin, uninterrupted
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xE6FFFFFF),
                                        Color(0x80FFFFFF),
                                        Color(0xE6FFFFFF)
                                    )
                                ),
                                shape = RoundedCornerShape(35.dp)
                            )
                            // Sunlight dispersion: a TINY seven-colour prismatic
                            // fringe at the very edge of the rim — the faint
                            // chromatic edge a real water drop shows in sun.
                            .border(
                                width = 1.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0x33FF3B30), // red
                                        Color(0x33FF9500), // orange
                                        Color(0x33FFD60A), // yellow
                                        Color(0x3334C759), // green
                                        Color(0x330A84FF), // blue
                                        Color(0x335E5CE6), // indigo
                                        Color(0x33BF5AF2), // violet
                                        Color(0x33FF3B30)  // wrap back to red
                                    )
                                ),
                                shape = RoundedCornerShape(35.dp)
                            )
                    )
                }
            }
        }
    }
}
