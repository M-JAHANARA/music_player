package com.jahanmusic.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.jahanmusic.app.data.DeviceSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single ExoPlayer instance shared by UI (Compose), Service, Widget, Notification.
 * Media3 ExoPlayer = real device file playback.
 */
enum class RepeatMode {
    ORDER,       // sequential, stop at end
    LOOP_ALL,    // repeat the whole queue
    SHUFFLE_ALL, // shuffle the whole queue
    REPEAT_ONE   // repeat the current song
}

object PlayerManager {

    private var player: ExoPlayer? = null

    private val _currentSong = MutableStateFlow<DeviceSong?>(null)
    val currentSong: StateFlow<DeviceSong?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _queue = MutableStateFlow<List<DeviceSong>>(emptyList())
    val queue: StateFlow<List<DeviceSong>> = _queue.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeatOne = MutableStateFlow(false)
    val repeatOne: StateFlow<Boolean> = _repeatOne.asStateFlow()

    // 4-mode repeat: order → loop all → shuffle all → repeat one → order …
    private val _repeatMode = MutableStateFlow(RepeatMode.ORDER)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    /** Cycle to the next repeat mode and APPLY it to the real player. */
    fun cycleRepeatMode(): RepeatMode {
        val next = when (_repeatMode.value) {
            RepeatMode.ORDER -> RepeatMode.LOOP_ALL
            RepeatMode.LOOP_ALL -> RepeatMode.SHUFFLE_ALL
            RepeatMode.SHUFFLE_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.ORDER
        }
        _repeatMode.value = next
        applyRepeatMode(next)
        return next
    }

    private fun applyRepeatMode(mode: RepeatMode) {
        val p = player
        _shuffle.value = mode == RepeatMode.SHUFFLE_ALL
        _repeatOne.value = mode == RepeatMode.REPEAT_ONE
        if (p == null) return
        p.shuffleModeEnabled = mode == RepeatMode.SHUFFLE_ALL
        p.repeatMode = when (mode) {
            RepeatMode.ORDER -> Player.REPEAT_MODE_OFF
            RepeatMode.LOOP_ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.SHUFFLE_ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun getOrCreate(context: Context): ExoPlayer {
        val existing = player
        if (existing != null) return existing
        val appCtx = context.applicationContext
        val newPlayer = ExoPlayer.Builder(appCtx).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (duration > 0) _durationMs.value = duration
                }
            })
            repeatMode = Player.REPEAT_MODE_OFF
            shuffleModeEnabled = false
        }
        player = newPlayer
        applyRepeatMode(_repeatMode.value)
        EqualizerController.attachToSession(appCtx, newPlayer.audioSessionId)
        return newPlayer
    }

    fun audioSessionId(): Int = player?.audioSessionId ?: 0

    fun setQueue(songs: List<DeviceSong>, startIndex: Int = 0) {
        val p = player ?: return
        _queue.value = songs
        p.setMediaItems(songs.map { MediaItem.fromUri(it.contentUri) }, startIndex, 0L)
        p.prepare()
        _currentSong.value = songs.getOrNull(startIndex)
        _durationMs.value = songs.getOrNull(startIndex)?.duration ?: 0L
    }

    fun playSong(context: Context, songs: List<DeviceSong>, index: Int) {
        val p = getOrCreate(context)
        if (_queue.value != songs) {
            _queue.value = songs
            p.setMediaItems(songs.map { MediaItem.fromUri(it.contentUri) }, index, 0L)
            p.prepare()
        } else {
            if (p.currentMediaItemIndex != index) p.seekTo(index, 0L)
        }
        _currentSong.value = songs.getOrNull(index)
        _durationMs.value = songs.getOrNull(index)?.duration ?: p.duration.coerceAtLeast(0L)
        p.play()
        _isPlaying.value = true
    }

    fun togglePlayPause(): Boolean {
        val p = player ?: return false
        return if (p.isPlaying) {
            p.pause()
            _isPlaying.value = false
            false
        } else {
            p.play()
            _isPlaying.value = true
            true
        }
    }

    fun next(): DeviceSong? {
        val p = player ?: return null
        if (!p.hasNextMediaItem() && _queue.value.isNotEmpty()) {
            p.seekTo(0, 0L)
            p.play()
            _currentSong.value = _queue.value.firstOrNull()
            return _currentSong.value
        }
        p.seekToNextMediaItem()
        p.play()
        _currentSong.value = _queue.value.getOrNull(p.currentMediaItemIndex)
        return _currentSong.value
    }

    fun previous(): DeviceSong? {
        val p = player ?: return null
        if (p.currentPosition > 3000) {
            p.seekTo(0L)
            return _currentSong.value
        }
        if (!p.hasPreviousMediaItem() && _queue.value.isNotEmpty()) {
            val last = _queue.value.size - 1
            p.seekTo(last, 0L)
            p.play()
            _currentSong.value = _queue.value.getOrNull(last)
            return _currentSong.value
        }
        p.seekToPreviousMediaItem()
        p.play()
        _currentSong.value = _queue.value.getOrNull(p.currentMediaItemIndex)
        return _currentSong.value
    }

    fun seekTo(ms: Long) {
        player?.seekTo(ms)
        _positionMs.value = ms
    }

    fun updatePosition() {
        val p = player ?: return
        _positionMs.value = p.currentPosition.coerceAtLeast(0L)
        if (p.duration > 0) _durationMs.value = p.duration
        _queue.value.getOrNull(p.currentMediaItemIndex)?.let {
            if (it != _currentSong.value) _currentSong.value = it
        }
    }

    fun toggleShuffle(): Boolean {
        val mode = if (_repeatMode.value == RepeatMode.SHUFFLE_ALL) RepeatMode.LOOP_ALL else RepeatMode.SHUFFLE_ALL
        _repeatMode.value = mode
        applyRepeatMode(mode)
        return _shuffle.value
    }

    fun toggleRepeat(): Boolean {
        val mode = if (_repeatMode.value == RepeatMode.REPEAT_ONE) RepeatMode.LOOP_ALL else RepeatMode.REPEAT_ONE
        _repeatMode.value = mode
        applyRepeatMode(mode)
        return _repeatOne.value
    }

    fun release() {
        try { player?.release() } catch (_: Exception) {}
        player = null
    }
}
