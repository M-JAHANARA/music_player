package com.jahanmusic.app.player

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * System audio effects bound to ExoPlayer audioSessionId.
 * 10 bands UI: 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k
 * (mapped onto device's actual band count).
 *
 * Vendored equivalent of compose-audio-controls Knob/VerticalSlider —
 * same interaction model, no extra Gradle dep needed for publish build.
 */
object EqualizerController {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var sessionId: Int = 0

    // 10 UI bands 0..1
    private val _bands = MutableStateFlow(List(10) { 0.5f })
    val bands: StateFlow<List<Float>> = _bands.asStateFlow()

    private val _bass = MutableStateFlow(0.6f)
    val bass: StateFlow<Float> = _bass.asStateFlow()

    private val _virtualizer = MutableStateFlow(0.45f)
    val virtualizerStrength: StateFlow<Float> = _virtualizer.asStateFlow()

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    fun attachToSession(context: Context, audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == sessionId) return
        release()
        sessionId = audioSessionId
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _enabled.value
                applyAllBands()
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _enabled.value
                setStrength((_bass.value * 1000).toInt().coerceIn(0, 1000).toShort())
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _enabled.value
                setStrength((_virtualizer.value * 1000).toInt().coerceIn(0, 1000).toShort())
            }
        } catch (_: Exception) {
            // Effects not supported on this device — UI still works
        }
    }

    private fun Equalizer.applyAllBands() {
        try {
            val n = numberOfBands.coerceAtLeast(1)
            val range = bandLevelRange // [min, max] in mB
            val span = (range[1] - range[0]).coerceAtLeast(1)
            for (i in 0 until n) {
                // map 10 UI bands -> N device bands (nearest)
                val uiIndex = (i * 10 / n).coerceIn(0, 9)
                val v = _bands.value[uiIndex]
                val level = (range[0] + v * span).toInt().toShort()
                setBandLevel(i.toShort(), level)
            }
        } catch (_: Exception) {}
    }

    fun setBand(index: Int, value: Float) {
        if (index !in 0..9) return
        val cur = _bands.value.toMutableList()
        cur[index] = value.coerceIn(0f, 1f)
        _bands.value = cur
        try { equalizer?.applyAllBands() } catch (_: Exception) {}
    }

    fun setBass(value: Float) {
        _bass.value = value.coerceIn(0f, 1f)
        try {
            bassBoost?.setStrength((_bass.value * 1000).toInt().coerceIn(0, 1000).toShort())
        } catch (_: Exception) {}
    }

    fun setVirtualizer(value: Float) {
        _virtualizer.value = value.coerceIn(0f, 1f)
        try {
            virtualizer?.setStrength((_virtualizer.value * 1000).toInt().coerceIn(0, 1000).toShort())
        } catch (_: Exception) {}
    }

    fun setEnabled(on: Boolean) {
        _enabled.value = on
        try {
            equalizer?.enabled = on
            bassBoost?.enabled = on
            virtualizer?.enabled = on
        } catch (_: Exception) {}
    }

    fun applyPreset(preset: String) {
        // Flat / Pop / Rock / Jazz / Bass / Treble — map to 10-band curves
        val curve: List<Float> = when (preset) {
            "Pop" -> listOf(0.45f, 0.55f, 0.6f, 0.55f, 0.5f, 0.5f, 0.55f, 0.6f, 0.6f, 0.65f)
            "Rock" -> listOf(0.7f, 0.65f, 0.55f, 0.45f, 0.4f, 0.5f, 0.6f, 0.7f, 0.75f, 0.8f)
            "Jazz" -> listOf(0.55f, 0.5f, 0.5f, 0.6f, 0.6f, 0.6f, 0.55f, 0.6f, 0.65f, 0.6f)
            "Bass" -> listOf(0.9f, 0.85f, 0.75f, 0.6f, 0.5f, 0.45f, 0.45f, 0.45f, 0.5f, 0.5f)
            "Treble" -> listOf(0.4f, 0.4f, 0.45f, 0.5f, 0.55f, 0.6f, 0.7f, 0.8f, 0.85f, 0.9f)
            else -> List(10) { 0.5f } // Flat
        }
        _bands.value = curve
        try { equalizer?.applyAllBands() } catch (_: Exception) {}
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        sessionId = 0
    }
}
