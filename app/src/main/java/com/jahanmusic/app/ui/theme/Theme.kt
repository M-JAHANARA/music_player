package com.jahanmusic.app.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.rememberAsyncImagePainter
import com.jahanmusic.app.data.ThemePrefs

// ── App colour theme (runtime-switchable) ─────────────────────────────────
// AccentPurple etc. become `val` getters so every existing screen keeps
// compiling unchanged while following the colour chosen on the appearance
// page (theme.png colour card). The three mutable states are the live
// sources of truth; ThemePrefs mirrors them into SharedPreferences.
object AccentState {
    val primary = mutableStateOf(Color(0xFFB388FF))
    val secondary = mutableStateOf(Color(0xFFE040FB))
}

/** Applies an accent pair instantly (no activity restart needed). */
fun applyAccent(primary: Color, secondary: Color) {
    AccentState.primary.value = primary
    AccentState.secondary.value = secondary
}

/** Accent presets shown on the appearance page (theme.png swatch row). */
data class AccentOption(val primary: Color, val secondary: Color)

val AccentOptions = listOf(
    AccentOption(Color(0xFFC5A3FF), Color(0xFFE040FB)), // بنفش (پیش‌فرض)
    AccentOption(Color(0xFF4A5CE8), Color(0xFF6C8CFF)), // آبی
    AccentOption(Color(0xFF8A4FD0), Color(0xFFB26BFF)), // بنفش تیره
    AccentOption(Color(0xFFC24BC7), Color(0xFFE040FB)), // صورتی
    AccentOption(Color(0xFFE5395F), Color(0xFFFF5252)), // قرمز
    AccentOption(Color(0xFF6E7B91), Color(0xFF8A97AC))  // خاکستری
)

val AccentPurple: Color get() = AccentState.primary.value
val AccentMagenta: Color get() = AccentState.secondary.value
val AccentPink: Color get() = AccentState.primary.value.copy(alpha = 0.85f)

// ── Live appearance state (shared with the appearance page) ───────────────
val AppBgId = mutableStateOf("bg1")          // "bg1".."bg8" / ThemePrefs.BG_CUSTOM
val AppAccentIndex = mutableIntStateOf(0)    // index into AccentOptions
val AppBlurLevel = mutableIntStateOf(70)     // 0..100

// Fixed dark base colours (page chrome stays readable over any wallpaper)
val DarkBackground = Color(0xFF0A0A1A)
val DarkSurface = Color(0xFF1A1A2E)
val DarkSurfaceVariant = Color(0xFF16213E)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentMagenta,
    tertiary = AccentPink,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun JahanMusicTheme(
    content: @Composable () -> Unit
) {
    // Follow accent changes live
    val colorScheme = DarkColorScheme.copy(
        primary = AccentPurple,
        secondary = AccentMagenta,
        tertiary = AccentPink
    )
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

/**
 * Draw the user's chosen background (bg1..bg8 drawable or a gallery pick)
 * with the configured blur, then a dark scrim so glass cards + text stay
 * readable. Reads the LIVE appearance state, so changing bg / blur on the
 * appearance page updates every screen instantly.
 */
@Composable
fun AppBackground(
    context: Context,
    modifier: Modifier = Modifier
) {
    val bgId = AppBgId.value
    val blurLevel = AppBlurLevel.intValue

    Box(modifier = modifier.fillMaxSize()) {
        if (bgId == ThemePrefs.BG_CUSTOM && ThemePrefs.customBgFile(context).exists()) {
            Image(
                painter = rememberAsyncImagePainter(ThemePrefs.customBgFile(context)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurLevel.dp * 0.45f),
                contentScale = ContentScale.Crop
            )
        } else {
            val resId = context.resources.getIdentifier(
                bgId.ifEmpty { "bg1" }, "drawable", context.packageName
            )
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(blurLevel.dp * 0.45f),
                    contentScale = ContentScale.Crop
                )
            }
        }
        // Readability scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xB30A0A1A),
                            Color(0x800A0A1A)
                        )
                    )
                )
        )
    }
}
