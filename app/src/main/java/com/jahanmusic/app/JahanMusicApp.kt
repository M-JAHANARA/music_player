package com.jahanmusic.app

import android.app.Application
import com.jahanmusic.app.data.ThemePrefs
import com.jahanmusic.app.player.PlayerManager

class JahanMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Restore the saved theme (accent + wallpaper + blur) before first frame
        try { ThemePrefs.bootstrap(this) } catch (_: Exception) {}
        // Pre-create player on app start (Android 12+ safe)
        try { PlayerManager.getOrCreate(this) } catch (_: Exception) {}
    }
}
