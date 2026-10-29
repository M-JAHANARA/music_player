package com.jahanmusic.app.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jahanmusic.app.data.DeviceAlbum
import com.jahanmusic.app.data.DeviceSong
import com.jahanmusic.app.data.MusicRepository
import com.jahanmusic.app.data.Prefs
import com.jahanmusic.app.player.EqualizerController
import com.jahanmusic.app.player.PlayerManager
import com.jahanmusic.app.player.RepeatMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _songs = MutableStateFlow<List<DeviceSong>>(emptyList())
    val songs: StateFlow<List<DeviceSong>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<DeviceAlbum>>(emptyList())
    val albums: StateFlow<List<DeviceAlbum>> = _albums.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    // Playback — delegated to PlayerManager (Media3 ExoPlayer singleton)
    val currentSong: StateFlow<DeviceSong?> = PlayerManager.currentSong
    val isPlaying: StateFlow<Boolean> = PlayerManager.isPlaying
    val positionMs: StateFlow<Long> = PlayerManager.positionMs
    val durationMs: StateFlow<Long> = PlayerManager.durationMs
    val shuffle: StateFlow<Boolean> = PlayerManager.shuffle
    val repeatOne: StateFlow<Boolean> = PlayerManager.repeatOne
    val repeatMode: StateFlow<RepeatMode> = PlayerManager.repeatMode

    // Equalizer UI state
    val eqBands = EqualizerController.bands
    val eqBass = EqualizerController.bass
    val eqVirtual = EqualizerController.virtualizerStrength

    private var progressJob: Job? = null

    init {
        // Keep position ticker running while playing
        progressJob = viewModelScope.launch {
            while (isActive) {
                PlayerManager.updatePosition()
                delay(500)
            }
        }
    }

    fun checkPermission(): Boolean {
        val context = getApplication<Application>()
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
        _hasPermission.value = granted
        if (granted) loadMusic()
        return granted
    }

    fun onPermissionGranted() {
        _hasPermission.value = true
        val context = getApplication<Application>()
        viewModelScope.launch {
            try { Prefs.setAccessGranted(context) } catch (_: Exception) {}
        }
        loadMusic()
    }

    fun loadMusic() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _songs.value = MusicRepository.loadSongs(context)
            _albums.value = MusicRepository.loadAlbums(context)
            // Init player queue so widget/notification have something
            if (_songs.value.isNotEmpty()) {
                try {
                    PlayerManager.getOrCreate(context)
                    // Don't autoplay — just prepare queue
                    if (PlayerManager.queue.value.isEmpty()) {
                        PlayerManager.setQueue(_songs.value, 0)
                        // pause immediately after prepare
                        PlayerManager.getOrCreate(context).pause()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun playAt(index: Int) {
        val context = getApplication<Application>()
        val list = _songs.value
        if (index in list.indices) PlayerManager.playSong(context, list, index)
    }

    fun playSong(song: DeviceSong) {
        val context = getApplication<Application>()
        val list = _songs.value
        val idx = list.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        PlayerManager.playSong(context, list.ifEmpty { listOf(song) }, idx)
    }

    fun togglePlayPause() = PlayerManager.togglePlayPause()
    fun next() = PlayerManager.next()
    fun previous() = PlayerManager.previous()
    fun seekTo(ms: Long) = PlayerManager.seekTo(ms)
    fun toggleShuffle() = PlayerManager.toggleShuffle()
    fun toggleRepeat() = PlayerManager.toggleRepeat()
    fun cycleRepeatMode(): RepeatMode = PlayerManager.cycleRepeatMode()

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        progressJob?.cancel()
        super.onCleared()
    }
}
