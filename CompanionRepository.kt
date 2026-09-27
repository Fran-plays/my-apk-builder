package com.petmorph.ai.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.petmorph.ai.R
import com.petmorph.ai.data.local.CompanionDao
import com.petmorph.ai.data.model.CompanionEntity
import com.petmorph.ai.data.model.CompanionType
import com.petmorph.ai.data.model.Personality
import com.petmorph.ai.data.model.VoiceConfig
import com.petmorph.ai.storage.StorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CompanionRepository(
    private val dao: CompanionDao,
    private val context: Context,
) {
    fun observeCompanions(): Flow<List<CompanionEntity>> = dao.observeAll()

    suspend fun get(id: String): CompanionEntity? = dao.getById(id)
    suspend fun count(): Int = dao.count()
    suspend fun save(entity: CompanionEntity) = dao.insert(entity)
    suspend fun update(entity: CompanionEntity) = dao.update(entity)

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        dao.getById(id)?.let { entity ->
            dao.delete(entity)
            File(entity.originalImagePath).delete()
            File(entity.processedImagePath).delete()
        }
    }

    suspend fun duplicate(id: String): CompanionEntity? = withContext(Dispatchers.IO) {
        val src = dao.getById(id) ?: return@withContext null
        val newId = java.util.UUID.randomUUID().toString()
        val newOriginal = copyFile(src.originalImagePath, "original_$newId.png")
        val newProcessed = copyFile(src.processedImagePath, "processed_$newId.png")
        val copy = src.copy(
            id = newId,
            name = src.name + " copy",
            originalImagePath = newOriginal,
            processedImagePath = newProcessed,
            isDemo = false,
        )
        dao.insert(copy)
        copy
    }

    private fun copyFile(path: String, name: String): String {
        val dst = File(context.filesDir, name)
        runCatching { File(path).copyTo(dst, overwrite = true) }
        return dst.absolutePath
    }

    /** Seeds the built-in demo companion "Mochi" on first run. */
    suspend fun seedDemoIfNeeded(storage: StorageProvider) = withContext(Dispatchers.IO) {
        if (dao.count() > 0) return@withContext
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.mochi_demo)
            ?: return@withContext
        val processed = storage.saveBitmap(bmp, "processed_demo_mochi.png")
        val original = storage.saveBitmap(bmp, "original_demo_mochi.png")
        dao.insert(
            CompanionEntity(
                name = "Mochi",
                type = CompanionType.CUTE_PET,
                personality = Personality.ENERGETIC,
                originalImagePath = original,
                processedImagePath = processed,
                voiceJson = CompanionEntity.voiceToJson(VoiceConfig(pitch = 1.35f)),
                isDemo = true,
            )
        )
    }
}
