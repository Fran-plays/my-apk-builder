package com.petmorph.ai

import com.petmorph.ai.data.model.CompanionEntity
import com.petmorph.ai.data.model.VoiceConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceConfigTest {
    @Test
    fun `voice config round trips through json`() {
        val v = VoiceConfig(rate = 1.3f, pitch = 0.8f, volume = 0.5f, muted = true)
        val json = CompanionEntity.voiceToJson(v)
        val back = CompanionEntity("", "", com.petmorph.ai.data.model.CompanionType.CUTE_PET,
            com.petmorph.ai.data.model.Personality.CUTE, "", "", json).voice()
        assertEquals(v, back)
    }

    @Test
    fun `invalid json falls back to defaults`() {
        val back = CompanionEntity("", "", com.petmorph.ai.data.model.CompanionType.CUTE_PET,
            com.petmorph.ai.data.model.Personality.CUTE, "", "", "garbage").voice()
        assertEquals(VoiceConfig(), back)
    }
}
