package com.petmorph.ai.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Local Android TTS implementation — the offline fallback so the app always
 * works without a cloud TTS API. Cloud providers can implement [TTSProvider].
 */
class TTSController(private val context: Context) : TTSProvider {
    @Volatile private var tts: TextToSpeech? = null
    @Volatile private var ready = false
    @Volatile private var onDone: (() -> Unit)? = null

    @Volatile var muted: Boolean = false
        set(v) { field = v; if (v) stop() }

    private var selectedVoice: Voice? = null

    override suspend fun initialize(): Boolean = withContext(Dispatchers.Main) {
        if (ready) return@withContext true
        try {
            tts = TextToSpeech(context) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    tts?.language = Locale.getDefault()
                    val voices = tts?.voices
                    selectedVoice = voices?.firstOrNull {
                        it.name.contains("female", true) && it.locale.language == Locale.getDefault().language
                    } ?: voices?.firstOrNull()
                    selectedVoice?.let { tts?.voice = it }
                }
            }
            kotlinx.coroutines.delay(600)
            ready
        } catch (e: Exception) { false }
    }

    override suspend fun speak(text: String, rate: Float, pitch: Float, volume: Float) {
        if (muted) return
        if (!ready) if (!initialize()) throw TTSUnavailableException()
        withContext(Dispatchers.Main) {
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            @Suppress("DEPRECATION")
            tts?.setOnUtteranceCompletedListener { onDone?.invoke() }
            val params = android.os.Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
            }
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "petmorph_utterance")
        }
    }

    override fun stop() = runCatching { tts?.stop() }
    override val isSpeaking: Boolean get() = runCatching { tts?.isSpeaking == true }.getOrDefault(false)
    override fun setOnDoneListener(listener: (() -> Unit)?) { onDone = listener }

    fun availableVoices(): List<String> = runCatching {
        tts?.voices?.map { it.name }?.distinct()?.sorted() ?: emptyList()
    }.getOrDefault(emptyList())

    fun shutdown() = runCatching { tts?.shutdown(); ready = false }
}
