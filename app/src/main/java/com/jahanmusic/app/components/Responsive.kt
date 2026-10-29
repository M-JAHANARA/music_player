package com.jahanmusic.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Responsive helpers — same layout scales on all Android 12+ phones + tablets. */
object Responsive {
    @Composable
    fun screenWidth(): Dp = LocalConfiguration.current.screenWidthDp.dp

    @Composable
    fun isCompact(): Boolean = LocalConfiguration.current.screenWidthDp < 600

    @Composable
    fun isTablet(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

    /** Adaptive horizontal padding: 16dp phones, 24dp tablets */
    @Composable
    fun hPadding(): Dp = if (isTablet()) 24.dp else 16.dp

    /** Category card size that stays square-ish on any density */
    @Composable
    fun categoryAspect(): Float = 1f
}
