package com.petmorph.ai.image

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

/**
 * Offline pipeline:
 *  1. Detect main subject (border flood-fill from image edges keyed on color
 *     similarity to corner pixels -> background mask).
 *  2. Remove background (alpha = 0 on mask), preserving the subject.
 *  3. Crop to the subject's alpha bounding box and resize to [maxSize].
 *
 * Honest note: this is a practical heuristic, not ML segmentation. For
 * subjects on busy backgrounds a cloud [ImageProcessor] should be injected.
 */
class LocalImageProcessor : ImageProcessor {

    override suspend fun removeBackground(bitmap: Bitmap): ProcessResult =
        withContext(Dispatchers.Default) {
            runCatching {
                val w = bitmap.width
                val h = bitmap.height
                if (w < 8 || h < 8) return@runCatching bitmap
                val pixels = IntArray(w * h)
                bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

                val bg = BooleanArray(w * h)
                val queue = ArrayDeque<Int>()
                val tolSq = TOLERANCE * TOLERANCE

                fun trySeed(x: Int, y: Int) {
                    val i = y * w + x
                    if (!bg[i]) { bg[i] = true; queue.add(i) }
                }
                for (x in 0 until w) { trySeed(x, 0); trySeed(x, h - 1) }
                for (y in 0 until h) { trySeed(0, y); trySeed(w - 1, y) }

                fun colorDistSq(a: Int, b: Int): Int {
                    val dr = ((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)
                    val dg = ((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)
                    val db = (a and 0xFF) - (b and 0xFF)
                    return dr * dr + dg * dg + db * db
                }

                while (queue.isNotEmpty()) {
                    val i = queue.poll()
                    val x = i % w
                    val y = i / w
                    val seed = pixels[i] and 0xFFFFFF
                    val neighbors = intArrayOf(
                        if (x > 0) i - 1 else -1,
                        if (x < w - 1) i + 1 else -1,
                        if (y > 0) i - w else -1,
                        if (y < h - 1) i + w else -1,
                    )
                    for (n in neighbors) {
                        if (n < 0 || bg[n]) continue
                        if (colorDistSq(pixels[n] and 0xFFFFFF, seed) <= tolSq ||
                            colorDistSq(pixels[n] and 0xFFFFFF, pixels[0] and 0xFFFFFF) <= tolSq
                        ) {
                            bg[n] = true
                            queue.add(n)
                        }
                    }
                }

                // If flood-fill swallowed almost everything, the image likely
                // has no clean background -> keep it as-is instead of garbage.
                val bgCount = bg.count { it }
                if (bgCount > (w * h) * 0.97) return@runCatching bitmap

                for (i in pixels.indices) {
                    if (bg[i]) pixels[i] = pixels[i] and 0x00FFFFFF
                }
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    .apply { setPixels(pixels, 0, w, 0, 0, w, h) }
            }.fold(
                onSuccess = { ProcessResult.Success(it) },
                onFailure = { ProcessResult.Error(it.message ?: "Background removal failed") }
            )
        }

    override suspend fun normalize(bitmap: Bitmap, maxSize: Int): Bitmap =
        withContext(Dispatchers.Default) {
            // Crop to opaque bounding box
            val w = bitmap.width
            val h = bitmap.height
            val px = IntArray(w * h)
            bitmap.getPixels(px, 0, w, 0, 0, w, h)
            var minX = w; var minY = h; var maxX = -1; var maxY = -1
            for (y in 0 until h) {
                for (x in 0 until w) {
                    if ((px[y * w + x] ushr 24) > 16) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
            val cropped = if (maxX >= minX && maxY >= minY) {
                Bitmap.createBitmap(bitmap, minX, minY, maxX - minX + 1, maxY - minY + 1)
            } else bitmap

            val scale = minOf(
                maxSize.toFloat() / cropped.width,
                maxSize.toFloat() / cropped.height,
                1.0f
            )
            if (scale >= 0.999f) cropped else Bitmap.createScaledBitmap(
                cropped, (cropped.width * scale).toInt().coerceAtLeast(1),
                (cropped.height * scale).toInt().coerceAtLeast(1), true
            )
        }

    companion object {
        // Color similarity tolerance (0..255 per channel, squared distance)
        const val TOLERANCE = 42
    }
}
