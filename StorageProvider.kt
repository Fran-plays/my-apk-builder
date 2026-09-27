package com.petmorph.ai.storage

import android.graphics.Bitmap
import java.io.File

interface StorageProvider {
    suspend fun saveBitmap(bitmap: Bitmap, fileName: String): String
    fun getFile(fileName: String): File
}

class LocalStorageProvider(private val context: android.content.Context) : StorageProvider {
    override suspend fun saveBitmap(bitmap: Bitmap, fileName: String): String {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file.absolutePath
    }

    override fun getFile(fileName: String): File = File(context.filesDir, fileName)
}
