package com.petmorph.ai.image

import android.graphics.Bitmap

sealed class ProcessResult {
    data class Success(val bitmap: Bitmap) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}

/**
 * Abstraction so a cloud provider (e.g. background-removal API) can be
 * plugged in later without touching call sites. [LocalImageProcessor] is the
 * offline fallback and always works.
 */
interface ImageProcessor {
    suspend fun removeBackground(bitmap: Bitmap): ProcessResult
    suspend fun normalize(bitmap: Bitmap, maxSize: Int = 512): Bitmap
}

class ProcessException(message: String) : Exception(message)
