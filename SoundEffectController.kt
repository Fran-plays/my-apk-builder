package com.petmorph.ai.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Plays short UI sound effects synthesized with [ToneGenerator] — generated
 * tones, not copyrighted samples. Each effect is a small tone sequence.
 */
class SoundEffectController(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile var enabled: Boolean = true
    @Volatile var volume: Float = 1.0f

    private val tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, (100 * volume).toInt().coerceIn(1, 100)) }.getOrNull()

    fun play(effect: SoundEffect) {
        if (!enabled || tone == null) return
        scope.launch {
            when (effect) {
                SoundEffect.CLICK -> beep(ToneGenerator.TONE_PROP_ACK, 60)
                SoundEffect.HAPPY -> {
                    beep(ToneGenerator.TONE_DTMF_5, 90); pause(70); beep(ToneGenerator.TONE_DTMF_9, 110)
                }
                SoundEffect.SURPRISE -> {
                    beep(ToneGenerator.TONE_DTMF_A, 80); pause(50); beep(ToneGenerator.TONE_DTMF_D, 140)
                }
                SoundEffect.JUMP -> {
                    beep(ToneGenerator.TONE_DTMF_3, 70); pause(40); beep(ToneGenerator.TONE_DTMF_6, 70); pause(40)
                    beep(ToneGenerator.TONE_DTMF_9, 120)
                }
                SoundEffect.SLEEP -> {
                    beep(ToneGenerator.TONE_DTMF_7, 160); pause(140); beep(ToneGenerator.TONE_DTMF_4, 220)
                }
                SoundEffect.ANGRY -> {
                    beep(ToneGenerator.TONE_SUP_ERROR, 180)
                }
                SoundEffect.NOTIFICATION -> beep(ToneGenerator.TONE_PROP_PROMPT, 160)
                SoundEffect.LEVEL_UP -> {
                    listOf(ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_5,
                        ToneGenerator.TONE_DTMF_8).forEach { beep(it, 80); pause(60) }
                }
                SoundEffect.ERROR -> beep(ToneGenerator.TONE_SUP_BUSY, 200)
            }
        }
    }

    private fun beep(toneType: Int, ms: Int) = runCatching { tone?.startTone(toneType, ms) }
    private suspend fun pause(ms: Long) = kotlinx.coroutines.delay(ms)

    fun release() = runCatching { tone?.release() }
}
