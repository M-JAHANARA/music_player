package com.jahanmusic.app.data

import android.content.Context
import android.net.Uri
import com.jahanmusic.app.ui.theme.AccentOptions
import com.jahanmusic.app.ui.theme.AppAccentIndex
import com.jahanmusic.app.ui.theme.AppBgId
import com.jahanmusic.app.ui.theme.AppBlurLevel
import com.jahanmusic.app.ui.theme.applyAccent
import java.io.File

/**
 * Appearance settings (theme.png page): chosen background, accent colour,
 * blur level. Stored in plain SharedPreferences so they can be read
 * synchronously before the first frame (no flash of default accent).
 */
object ThemePrefs {
    const val BG_CUSTOM = "custom"
    private const val DEFAULT_BG = "bg1"
    private const val DEFAULT_BLUR = 70

    private const val NAME = "jahan_theme"
    private const val KEY_BG = "bg_id"
    private const val KEY_ACCENT = "accent_index"
    private const val KEY_BLUR = "blur_level"

    private fun sp(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    // ── Background ────────────────────────────────────────────────────────
    // "bg1".."bg8" = bundled drawables, BG_CUSTOM = user's gallery copy in
    // internal storage (copied on pick — survives reboots without persisted
    // content-uri permissions).
    fun bgId(context: Context): String =
        sp(context).getString(KEY_BG, DEFAULT_BG) ?: DEFAULT_BG

    fun setBgId(context: Context, id: String) {
        sp(context).edit().putString(KEY_BG, id).apply()
    }

    fun customBgFile(context: Context): File =
        File(context.filesDir, "custom_background.img")

    /** Copies the picked gallery image into internal storage. */
    fun saveCustomBg(context: Context, uri: Uri): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            customBgFile(context).outputStream().use { input.copyTo(it) }
        } != null
    } catch (_: Exception) {
        false
    }

    /** All thumbnail slots for the picker row: gallery pick FIRST, then bg1..bg8. */
    fun bgSlots(context: Context): List<String> = buildList {
        if (customBgFile(context).exists()) add(BG_CUSTOM)
        addAll((1..8).map { "bg$it" })
    }

    // ── App colour (accent) ───────────────────────────────────────────────
    fun accentIndex(context: Context): Int =
        sp(context).getInt(KEY_ACCENT, 0)

    fun setAccentIndex(context: Context, index: Int) {
        sp(context).edit().putInt(KEY_ACCENT, index).apply()
    }

    // ── Background blur level (0..100) ────────────────────────────────────
    fun blurLevel(context: Context): Int =
        sp(context).getInt(KEY_BLUR, DEFAULT_BLUR)

    fun setBlurLevel(context: Context, level: Int) {
        sp(context).edit().putInt(KEY_BLUR, level.coerceIn(0, 100)).apply()
    }

    /**
     * Restore the saved theme into Theme.kt state — call once at app start so
     * accent / background / blur come back exactly as the user left them.
     */
    fun bootstrap(context: Context) {
        val idx = accentIndex(context).coerceIn(0, AccentOptions.lastIndex)
        val option = AccentOptions[idx]
        AppAccentIndex.intValue = idx
        applyAccent(option.primary, option.secondary)
        AppBgId.value = bgId(context)
        AppBlurLevel.intValue = blurLevel(context).coerceIn(0, 100)
    }
}
