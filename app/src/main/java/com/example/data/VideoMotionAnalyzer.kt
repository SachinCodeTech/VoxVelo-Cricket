package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

object VideoMotionAnalyzer {
  suspend fun suggestFrames(context: Context, videoUri: Uri): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    var retriever: MediaMetadataRetriever? = null
    try {
      retriever = MediaMetadataRetriever()
      retriever.setDataSource(context, videoUri)
      val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: return@withContext null
      if (durationMs <= 0) return@withContext null

      val sampleCount = 28
      val stepUs = (durationMs * 1000L) / sampleCount
      val frames = mutableListOf<Pair<Double, FloatArray>>()

      for (i in 0..sampleCount) {
        val timeUs = i * stepUs
        val bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: continue
        val scaled = Bitmap.createScaledBitmap(bmp, 48, 27, false)
        val pixels = IntArray(48 * 27)
        scaled.getPixels(pixels, 0, 48, 0, 0, 48, 27)
        val gray = FloatArray(pixels.size) { p ->
          val color = pixels[p]
          val r = (color shr 16) and 0xFF
          val g = (color shr 8) and 0xFF
          val b = color and 0xFF
          (r + g + b) / 3f
        }
        frames.add(Pair(timeUs / 1_000_000.0, gray))
        if (scaled != bmp) scaled.recycle()
        bmp.recycle()
      }

      if (frames.size < 4) return@withContext null

      val motion = mutableListOf<Pair<Double, Float>>()
      for (i in 1 until frames.size) {
        val prev = frames[i - 1].second
        val curr = frames[i].second
        var diff = 0f
        val len = minOf(prev.size, curr.size)
        for (p in 0 until len) {
          diff += abs(curr[p] - prev[p])
        }
        motion.add(Pair(frames[i].first, diff))
      }

      val mid = (motion.size * 0.5).toInt().coerceAtLeast(1)
      val releasePeak = motion.take(mid).maxByOrNull { it.second }
      val arrivalPeak = motion.drop(mid).maxByOrNull { it.second }

      if (releasePeak != null && arrivalPeak != null) {
        Pair(releasePeak.first, arrivalPeak.first)
      } else null
    } catch (e: Exception) {
      null
    } finally {
      try { retriever?.release() } catch (_: Exception) {}
    }
  }

  fun detectFps(context: Context, videoUri: Uri): Double? {
    var retriever: MediaMetadataRetriever? = null
    return try {
      retriever = MediaMetadataRetriever()
      retriever.setDataSource(context, videoUri)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toDoubleOrNull()
      } else null
    } catch (e: Exception) {
      null
    } finally {
      try { retriever?.release() } catch (_: Exception) {}
    }
  }
}
