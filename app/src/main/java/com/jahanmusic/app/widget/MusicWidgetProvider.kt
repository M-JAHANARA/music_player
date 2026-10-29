package com.jahanmusic.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jahanmusic.app.MainActivity
import com.jahanmusic.app.R
import com.jahanmusic.app.player.PlayerManager
import com.jahanmusic.app.service.MusicService

/**
 * Home-screen + lock-screen widget matching notification_widget.jpg
 * (prev/play/next only — bottom-left extra icon removed per spec).
 */
class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        var changed = false
        when (intent.action) {
            "com.jahanmusic.app.ACTION_PLAY_PAUSE" -> {
                try {
                    PlayerManager.getOrCreate(context)
                    PlayerManager.togglePlayPause()
                    changed = true
                } catch (_: Exception) {}
            }
            "com.jahanmusic.app.ACTION_NEXT" -> {
                try {
                    PlayerManager.getOrCreate(context)
                    PlayerManager.next()
                    changed = true
                } catch (_: Exception) {}
            }
            "com.jahanmusic.app.ACTION_PREV" -> {
                try {
                    PlayerManager.getOrCreate(context)
                    PlayerManager.previous()
                    changed = true
                } catch (_: Exception) {}
            }
        }
        if (changed) {
            // refresh all widgets
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                android.content.ComponentName(context, MusicWidgetProvider::class.java)
            )
            for (id in ids) updateAppWidget(context, mgr, id)
            // also refresh notification
            try {
                context.startForegroundService(Intent(context, MusicService::class.java))
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            val song = try { PlayerManager.currentSong.value } catch (_: Exception) { null }
            val playing = try { PlayerManager.isPlaying.value } catch (_: Exception) { false }

            views.setTextViewText(R.id.widget_song_title, song?.title ?: "Jahan Music")
            views.setTextViewText(R.id.widget_artist_name, song?.artist ?: "Feel The Music")
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPending = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_album_art, mainPending)

            fun bcast(action: String, req: Int): PendingIntent {
                val i = Intent(context, MusicWidgetProvider::class.java).setAction(action)
                return PendingIntent.getBroadcast(context, req, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }

            views.setOnClickPendingIntent(R.id.widget_play_pause, bcast("com.jahanmusic.app.ACTION_PLAY_PAUSE", 10))
            views.setOnClickPendingIntent(R.id.widget_next, bcast("com.jahanmusic.app.ACTION_NEXT", 11))
            views.setOnClickPendingIntent(R.id.widget_prev, bcast("com.jahanmusic.app.ACTION_PREV", 12))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
