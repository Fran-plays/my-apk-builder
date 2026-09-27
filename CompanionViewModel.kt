package com.petmorph.ai.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petmorph.ai.core.AppContainer
import com.petmorph.ai.PetMorphApp
import com.petmorph.ai.ai.ChatResult
import com.petmorph.ai.data.model.CompanionEntity
import com.petmorph.ai.data.model.CompanionType
import com.petmorph.ai.data.model.Personality
import com.petmorph.ai.data.model.VoiceConfig
import com.petmorph.ai.image.ProcessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ProcessingState {
    data object Idle : ProcessingState()
    data object Loading : ProcessingState()
    data class Success(val companionId: String) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}

class CompanionViewModel(app: Application) : AndroidViewModel(app) {
    val container: AppContainer = (app as PetMorphApp).container

    val companions: StateFlow<List<CompanionEntity>> =
        container.repository.observeCompanions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val processing = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val talking = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            container.repository.seedDemoIfNeeded(container.storage)
        }
    }

    fun createFromImage(uri: Uri, name: String, type: CompanionType, personality: Personality) {
        viewModelScope.launch {
            processing.value = ProcessingState.Loading
            try {
                val id = withContext(Dispatchers.IO) {
                    val original = copyUriToInternal(uri, "original_${System.currentTimeMillis()}.png")
                    val src = BitmapFactory.decodeFile(original)
                        ?: throw IllegalStateException("Could not decode image")
                    if (src.width * src.height > 4000 * 4000) throw IllegalStateException("Image too large")

                    val noBg = when (val r = container.imageProcessor.removeBackground(src)) {
                        is ProcessResult.Success -> r.bitmap
                        is ProcessResult.Error -> throw IllegalStateException(r.message)
                    }
                    val normalized = container.imageProcessor.normalize(noBg)
                    val processed = container.storage.saveBitmap(normalized, "processed_${System.currentTimeMillis()}.png")
                    src.recycle()

                    val entity = CompanionEntity(
                        name = name, type = type, personality = personality,
                        originalImagePath = original, processedImagePath = processed,
                        voiceJson = CompanionEntity.voiceToJson(VoiceConfig()),
                    )
                    container.repository.save(entity)
                    entity.id
                }
                processing.value = ProcessingState.Success(id)
            } catch (e: Exception) {
                processing.value = ProcessingState.Error(e.message ?: "Processing failed")
            }
        }
    }

    private suspend fun copyUriToInternal(uri: Uri, fileName: String): String =
        withContext(Dispatchers.IO) {
            val input = getApplication<Application>().contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot read selected image")
            val file = java.io.File(getApplication<Application>().filesDir, fileName)
            input.use { i -> file.outputStream().use { o -> i.copyTo(o) } }
            file.absolutePath
        }

    fun loadBitmap(path: String): Bitmap? = runCatching {
        BitmapFactory.decodeFile(path)
    }.getOrNull()

    fun updateCompanion(entity: CompanionEntity) = viewModelScope.launch {
        container.repository.update(entity)
    }

    fun rename(id: String, newName: String) = viewModelScope.launch {
        container.repository.get(id)?.let { container.repository.update(it.copy(name = newName)) }
    }

    fun duplicate(id: String) = viewModelScope.launch {
        container.repository.duplicate(id)
    }

    fun delete(id: String) = viewModelScope.launch {
        container.repository.delete(id)
    }

    /** AI chat with offline fallback; optionally speaks the reply. */
    fun chat(entity: CompanionEntity, message: String, onReply: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            val result = container.ai.chat(entity.name, entity.personality, message)
            when (result) {
                is ChatResult.Success -> {
                    onReply(result.reply, result.fromCloud)
                    speak(entity, result.reply)
                }
                is ChatResult.Error -> {
                    container.sounds.play(com.petmorph.ai.audio.SoundEffect.ERROR)
                    onReply("Hmm, my thoughts got tangled. Try again?", false)
                }
            }
        }
    }

    fun speak(entity: CompanionEntity, text: String) {
        val v = entity.voice()
        if (v.muted) return
        viewModelScope.launch {
            try {
                talking.value = true
                container.tts.setOnDoneListener { talking.value = false }
                container.tts.speak(text, v.rate, v.pitch, v.volume)
            } catch (e: Exception) {
                talking.value = false
            }
        }
    }

    fun setTalkState(t: Boolean) { talking.value = t }

    override fun onCleared() {
        container.tts.stop()
        super.onCleared()
    }
}
