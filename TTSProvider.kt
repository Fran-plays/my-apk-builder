package com.petmorph.ai.tts

interface TTSProvider {
    /** Called once before any [speak]. Result: true when a voice is ready. */
    suspend fun initialize(): Boolean
    suspend fun speak(text: String, rate: Float, pitch: Float, volume: Float)
    fun stop()
    val isSpeaking: Boolean
    fun setOnDoneListener(listener: (() -> Unit)?)
}

class TTSUnavailableException : Exception("No text-to-speech engine available on this device")
