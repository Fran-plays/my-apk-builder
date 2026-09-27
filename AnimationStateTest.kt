package com.petmorph.ai

import com.petmorph.ai.animation.AnimationState
import org.junit.Assert.assertEquals
import org.junit.Test

class AnimationStateTest {
    @Test
    fun `all required animation states exist`() {
        val required = listOf("IDLE", "HAPPY", "SAD", "ANGRY", "SURPRISED", "SLEEP",
            "WAVE", "JUMP", "BOUNCE", "DANCE", "TALK", "EXCITED", "CLICK")
        required.forEach { name ->
            assertEquals(name, AnimationState.valueOf(name).name)
        }
        assertEquals(13, AnimationState.entries.size)
    }

    @Test
    fun `every state has a human label`() {
        AnimationState.entries.forEach { assert(it.label.isNotBlank()) }
    }
}
