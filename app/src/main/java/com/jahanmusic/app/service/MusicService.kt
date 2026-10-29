package com.jahanmusic.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.jahanmusic.app.MainActivity
import com.jahanmusic.app.R
import com.jahanmusic.app.player.PlayerManager

/**
 * Media3 MediaSessionService:
 * - lock screen + notification + widget + Bluetooth all work
 * - custom RemoteViews matches notification_widget.jpg (no bottom-left extra icon)
 */
class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "jahan_music_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "com.jahanmusic.app.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.jahanmusic.app.ACTION_NEXT"
        const val ACTION_PREV = "com.jahanmusic.app.ACTION_PREV"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        try {
            val player = PlayerManager.getOrCreate(this)
            mediaSession = MediaSession.Builder(this, player).build()
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateNotification()
                }
            })
        } catch (_: Exception) {}
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> PlayerManager.togglePlayPause()
            ACTION_NEXT -> PlayerManager.next()
            ACTION_PREV -> PlayerManager.previous()
        }
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Jahan Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun actionIntent(action: String, req: Int): PendingIntent {
        val i = Intent(this, MusicService::class.java).setAction(action)
        return PendingIntent.getService(this, req, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildNotification(): Notification {
        val song = PlayerManager.currentSong.value
        val playing = PlayerManager.isPlaying.value
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Custom collapsed view = notification_widget.jpg without bottom-left icon
        val views = RemoteViews(packageName, R.layout.notification_widget)
        views.setTextViewText(R.id.notif_title, song?.title ?: "Blinding Lights")
        views.setTextViewText(R.id.notif_artist, song?.artist ?: "The Weeknd")
        views.setImageViewResource(
            R.id.notif_play_pause,
            if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        views.setOnClickPendingIntent(R.id.notif_play_pause, actionIntent(ACTION_PLAY_PAUSE, 1))
        views.setOnClickPendingIntent(R.id.notif_next, actionIntent(ACTION_NEXT, 2))
        views.setOnClickPendingIntent(R.id.notif_prev, actionIntent(ACTION_PREV, 3))
        // tap on art opens app
        views.setOnClickPendingIntent(R.id.notif_album_art, openApp)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song?.title ?: "Jahan Music")
            .setContentText(song?.artist ?: "Feel The Music")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openApp)
            .setCustomContentView(views)
            .addAction(android.R.drawable.ic_media_previous, "Prev", actionIntent(ACTION_PREV, 3))
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "Pause" else "Play",
                actionIntent(ACTION_PLAY_PAUSE, 1)
            )
            .addAction(android.R.drawable.ic_media_next, "Next", actionIntent(ACTION_NEXT, 2))
            .setOngoing(playing)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.notify(NOTIFICATION_ID, buildNotification())
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
