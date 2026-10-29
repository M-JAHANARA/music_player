package com.jahanmusic.app.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates a KnobMan-style horizontal image strip (32 frames) for
 * compose-audio-controls' [org.androidaudioplugin.composeaudiocontrols.ImageStripKnob]:
 * dark glass knob body, accent ring, a 270° track whose active arc and pointer
 * sweep with the value. Frame 0 = min value, last frame = max value.
 */
fun createKnobStrip(accentColor: Int, frames: Int = 32, frameSizePx: Int = 192): ImageBitmap {
    val strip = Bitmap.createBitmap(frameSizePx * frames, frameSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(strip)
    val c = frameSizePx / 2f
    val r = frameSizePx * 0.40f
    val stroke = frameSizePx * 0.045f

    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.rgb(36, 36, 58)
    }
    val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke * 0.8f
        color = android.graphics.Color.argb(90, 255, 255, 255)
    }
    val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = android.graphics.Color.argb(70, 255, 255, 255)
    }
    val active = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = accentColor
    }
    val pointer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke * 0.9f
        strokeCap = Paint.Cap.ROUND
        color = android.graphics.Color.rgb(235, 235, 245)
    }
    val arcRect = RectF(c - r, c - r, c + r, c + r)

    for (frame in 0 until frames) {
        val t = frame / (frames - 1f)
        canvas.save()
        canvas.translate(frame * frameSizePx.toFloat(), 0f)
        canvas.drawCircle(c, c, r, body)
        canvas.drawCircle(c, c, r, rim)
        // 270° track: starts lower-left (135°), sweeps clockwise to lower-right
        canvas.drawArc(arcRect, 135f, 270f, false, track)
        canvas.drawArc(arcRect, 135f, 270f * t, false, active)
        val angle = Math.toRadians((135.0 + 270.0 * t))
        val dirX = cos(angle).toFloat()
        val dirY = sin(angle).toFloat()
        canvas.drawLine(
            c + dirX * r * 0.55f, c + dirY * r * 0.55f,
            c + dirX * r * 0.82f, c + dirY * r * 0.82f,
            pointer
        )
        canvas.restore()
    }
    return strip.asImageBitmap()
}
