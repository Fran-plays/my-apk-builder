package com.petmorph.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class CompanionType { CUTE_PET, ANIME_CHARACTER, MASCOT, VTUBER_MINI }

enum class Personality(val label: String, val systemPrompt: String) {
    CUTE("Cute", "You are an adorable, sweet companion. Use gentle, affectionate language, emojis, and short sentences. Always be kind and encouraging."),
    FUNNY("Funny", "You are a playful, witty companion. Make light jokes, puns, and tease the user gently. Keep it family-friendly."),
    CALM("Calm", "You are a serene, thoughtful companion. Speak slowly and softly with reassuring, zen-like sentences."),
    ENERGETIC("Energetic", "You are a hyper, excited companion full of energy. Use exclamation marks, enthusiasm, and upbeat language."),
    TSUNDERE("Tsundere", "You act aloof and tsundere: deny that you care, but secretly show you do. Use phrases like 'hmph' and 'i-it's not like i like you or anything'. Keep it playful."),
    FRIENDLY("Friendly", "You are a warm, supportive best-friend companion. Be encouraging, curious about the user's day, and conversational."),
    CHAOTIC("Chaotic", "You are an unpredictable, chaotic companion. Give surprising, random but harmless answers. Be silly."),
}

data class VoiceConfig(
    val rate: Float = 1.0f,
    val pitch: Float = 1.2f,
    val volume: Float = 1.0f,
    val muted: Boolean = false,
)

@Entity(tableName = "companions")
data class CompanionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: CompanionType,
    val personality: Personality,
    val originalImagePath: String,
    val processedImagePath: String,
    val voiceJson: String,
    val soundEnabled: Boolean = true,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun voice(): VoiceConfig = runCatching {
        val p = voiceJson.split(",")
        VoiceConfig(
            rate = p.getOrElse(0) { "1.0" }.toFloat(),
            pitch = p.getOrElse(1) { "1.2" }.toFloat(),
            volume = p.getOrElse(2) { "1.0" }.toFloat(),
            muted = p.getOrElse(3) { "false" }.toBoolean(),
        )
    }.getOrDefault(VoiceConfig())

    companion object {
        fun voiceToJson(v: VoiceConfig) = "${v.rate},${v.pitch},${v.volume},${v.muted}"
    }
}
