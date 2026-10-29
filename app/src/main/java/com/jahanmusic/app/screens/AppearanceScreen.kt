package com.jahanmusic.app.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.jahanmusic.app.R
import com.jahanmusic.app.components.glassCard
import com.jahanmusic.app.data.ThemePrefs
import com.jahanmusic.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Appearance page (theme.png), top → bottom:
 * 1. header: back arrow at the TOP-LEFT, then the page name (same as settings)
 * 2. blur-glass card: background theme — ready wallpapers (bg1..bg8, bg1 first)
 *    + a real "choose from gallery" row; a gallery pick is saved and put FIRST
 * 3. blur-glass card: application colour theme (accent swatches)
 * 4. blur-glass card: background blur level — circular-thumb bar whose live
 *    percentage is shown on the card.
 *
 * Layout direction is RTL (Persian paragraphs), while rows that must read
 * left→right (header, thumbnail strip, swatch strip, gallery row) are forced
 * LTR — exactly how theme.png draws them.
 */
@Composable
fun AppearanceScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    // "Apply" confirmation on the glass button (auto-resets)
    var applied by remember { mutableStateOf(false) }
    LaunchedEffect(applied) {
        if (applied) {
            delay(2200)
            applied = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Live chosen wallpaper + blur + readability scrim
                AppBackground(context)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Header: arrow FIRST at the top-left, then the page name
                    // (row forced LTR so the arrow sits on the left, like settings)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                            }
                            Text(
                                text = "تنظیمات ظاهری",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 28.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        BackgroundThemeCard(context)
                        Spacer(modifier = Modifier.height(16.dp))
                        AppColorThemeCard(context)
                        Spacer(modifier = Modifier.height(16.dp))
                        BackgroundBlurCard(context)
                        Spacer(modifier = Modifier.height(18.dp))
                        ApplyChangesButton(
                            applied = applied,
                            onClick = {
                                // Commit every appearance choice: written to
                                // SharedPreferences (restored on next launch)
                                ThemePrefs.setBgId(context, AppBgId.value)
                                ThemePrefs.setAccentIndex(context, AppAccentIndex.intValue)
                                ThemePrefs.setBlurLevel(context, AppBlurLevel.intValue)
                                applied = true
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ── 1) Background theme card ────────────────────────────────────────────────
@Composable
private fun BackgroundThemeCard(context: Context) {
    // gallery pick FIRST, then bg1..bg8 (bg1 = the bundled default wallpaper)
    var slots by remember { mutableStateOf(ThemePrefs.bgSlots(context)) }
    val selectedBg = AppBgId.value

    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && ThemePrefs.saveCustomBg(context, uri)) {
            ThemePrefs.setBgId(context, ThemePrefs.BG_CUSTOM)
            AppBgId.value = ThemePrefs.BG_CUSTOM
            slots = ThemePrefs.bgSlots(context) // the picked image goes on top
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Header: text first (left-aligned), icon LAST → icon sits on the LEFT
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "از بین تصاویر آماده یکی را انتخاب کنید یا از گالری خود استفاده کنید.",
                fontSize = 13.5.sp,
                color = TextPrimary.copy(alpha = 0.92f),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "پس‌زمینه",
                tint = AccentPurple,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ready wallpapers — LTR so the FIRST slot is on the LEFT (theme.png)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slots.forEach { id ->
                    BackgroundThumb(
                        id = id,
                        selected = id == selectedBg,
                        onClick = {
                            ThemePrefs.setBgId(context, id)
                            AppBgId.value = id
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real gallery picker row (icon → label → chevron on the right)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x2E12122A))
                    .border(1.dp, Color(0x33B388FF), RoundedCornerShape(16.dp))
                    .clickable {
                        galleryPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "انتخاب از گالری",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BackgroundThumb(id: String, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current

    // Box kept LTR so the selected badge lands in the TOP-RIGHT corner
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 82.dp)
                .then(
                    if (selected) Modifier.shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = AccentPurple,
                        spotColor = AccentPurple
                    ) else Modifier
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141328))
                .then(
                    if (selected) Modifier.border(
                        width = 2.5.dp,
                        color = AccentPurple,
                        shape = RoundedCornerShape(16.dp)
                    ) else Modifier
                )
                .clickable(onClick = onClick)
        ) {
            if (id == ThemePrefs.BG_CUSTOM) {
                Image(
                    painter = rememberAsyncImagePainter(ThemePrefs.customBgFile(context)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val resId = remember(id) {
                    context.resources.getIdentifier(id, "drawable", context.packageName)
                }
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Select badge (top-right): filled violet + check, or an empty ring
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) AccentPurple else Color(0x59000000))
                    .border(
                        width = 1.5.dp,
                        color = if (selected) AccentPurple.copy(alpha = 0f) else Color(0xB3FFFFFF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "انتخاب شده",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// ── 2) Application colour theme card ────────────────────────────────────────
@Composable
private fun AppColorThemeCard(context: Context) {
    val selectedIndex = AppAccentIndex.intValue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        CardHeader(
            icon = Icons.Default.Palette,
            title = "رنگ برنامه",
            subtitle = "رنگهای اصلی رابط کاربری را انتخاب کنید."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Swatch strip: first swatch on the LEFT (theme.png), scrollable
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccentOptions.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.5.dp,
                                    color = option.primary,
                                    shape = CircleShape
                                ) else Modifier
                            )
                            .clickable {
                                AppAccentIndex.intValue = index
                                applyAccent(option.primary, option.secondary)
                                ThemePrefs.setAccentIndex(context, index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(option.primary, option.secondary)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

// ── 3) Background blur level card ───────────────────────────────────────────
@Composable
private fun BackgroundBlurCard(context: Context) {
    val blurLevel = AppBlurLevel.intValue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        CardHeader(
            icon = Icons.Default.WaterDrop,
            title = "میزان تاری پس‌زمینه",
            subtitle = "میزان محو شدن پس‌زمینه را تنظیم کنید.",
            // First child in an RTL row → sits at the far RIGHT (theme.png)
            trailing = {
                Text(
                    text = persianNumber(blurLevel) + "٪",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        BlurSlider(
            value = blurLevel,
            onValueChange = { level ->
                AppBlurLevel.intValue = level
                ThemePrefs.setBlurLevel(context, level)
            }
        )
    }
}

/** Circular-thumb bar (theme.png): gradient fill + round knob, RTL-safe. */
@Composable
private fun BlurSlider(value: Int, onValueChange: (Int) -> Unit) {
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { 11.dp.toPx() }
    val trackHeightPx = with(density) { 9.dp.toPx() }
    val haloPx = with(density) { 5.dp.toPx() }
    val ringPx = with(density) { 3.dp.toPx() }

    val primary = AccentPurple
    val secondary = AccentMagenta
    val onValueChangeState = rememberUpdatedState(onValueChange)

    fun valueFromX(x: Float, widthPx: Int): Int {
        val usable = (widthPx - 2f * thumbRadiusPx).coerceAtLeast(1f)
        return (((x - thumbRadiusPx) / usable).coerceIn(0f, 1f) * 100f).roundToInt()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var decided = false
                    var horizontal = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        if (!decided) {
                            val dx = abs(change.position.x - down.position.x)
                            val dy = abs(change.position.y - down.position.y)
                            if (dx > viewConfiguration.touchSlop || dy > viewConfiguration.touchSlop) {
                                decided = true
                                horizontal = dx >= dy
                            }
                        }
                        if (horizontal) {
                            onValueChangeState.value(
                                valueFromX(change.position.x, size.width)
                            )
                            change.consume()
                        }
                    }
                    // A clean tap seeks straight to the touched position
                    if (!decided) {
                        onValueChangeState.value(valueFromX(down.position.x, size.width))
                    }
                }
            }
            .drawBehind {
                val cy = size.height / 2f
                val half = trackHeightPx / 2f
                val usable = (size.width - 2f * thumbRadiusPx).coerceAtLeast(1f)
                val fraction = value.coerceIn(0, 100) / 100f
                val knobX = thumbRadiusPx + usable * fraction

                // Resting track
                drawRoundRect(
                    color = Color(0x40FFFFFF),
                    topLeft = Offset(0f, cy - half),
                    size = Size(size.width, trackHeightPx),
                    cornerRadius = CornerRadius(half, half)
                )
                // Filled part (gradient pinned to the full track width)
                if (fraction > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primary, secondary),
                            startX = 0f,
                            endX = size.width
                        ),
                        topLeft = Offset(0f, cy - half),
                        size = Size(knobX, trackHeightPx),
                        cornerRadius = CornerRadius(half, half)
                    )
                }
                // Round knob: soft halo → light ring → gradient core
                drawCircle(
                    color = secondary.copy(alpha = 0.30f),
                    radius = thumbRadiusPx + haloPx,
                    center = Offset(knobX, cy)
                )
                drawCircle(
                    color = Color(0xFFE7DBFF),
                    radius = thumbRadiusPx,
                    center = Offset(knobX, cy)
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(primary, secondary),
                        start = Offset(knobX - thumbRadiusPx, cy - thumbRadiusPx),
                        end = Offset(knobX + thumbRadiusPx, cy + thumbRadiusPx)
                    ),
                    radius = thumbRadiusPx - ringPx,
                    center = Offset(knobX, cy)
                )
            }
    )
}

// ── Shared card header (icon on the LEFT, text flush-left, like theme.png) ──
@Composable
private fun CardHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // First child of an RTL row → far RIGHT
        if (trailing != null) {
            trailing()
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = TextSecondary,
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Last child of an RTL row → far LEFT
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AccentPurple,
            modifier = Modifier.size(30.dp)
        )
    }
}

// ── Apply button ───────────────────────────────────────────────────────────
/**
 * Coloured glass button below the blur card.
 *
 * Commits the chosen background + accent colour + blur level:
 *  - ThemePrefs stores them, so they are restored on every app launch
 *  - the global states (AppBgId / AppAccentIndex / AppBlurLevel / AccentState)
 *    already drive every screen, so the look applies to the whole app.
 */
@Composable
private fun ApplyChangesButton(applied: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = AccentPurple,
                spotColor = AccentMagenta
            )
            .clip(shape)
            // Colour layer: current accent as a vivid gradient
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AccentPurple.copy(alpha = 0.92f),
                        AccentMagenta.copy(alpha = 0.88f)
                    )
                )
            )
            // Glass layer: soft top sheen like the other cards
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.12f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.12f))
                ),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (applied) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (applied) "در کل برنامه اعمال شد ✓" else "اعمال تغییرات در کل برنامه",
                color = Color.White,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
private val PersianDigits = charArrayOf(
    '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'
)

private fun persianNumber(value: Int): String =
    value.toString().map { c ->
        if (c in '0'..'9') PersianDigits[c - '0'] else c
    }.joinToString("")
