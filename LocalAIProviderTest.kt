package com.petmorph.ai

import com.petmorph.ai.ai.HybridAIProvider
import com.petmorph.ai.data.model.Personality
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAIProviderTest {
    private val responder = HybridAIProvider(
        com.petmorph.ai.network.ApiClient("http://localhost/"),
        null
    )

    @Test
    fun `every personality returns a non-blank offline reply`() {
        Personality.entries.forEach { p ->
            val r = responder.localReply(p, "hello there")
            assertTrue("personality " + p.name, r.isNotBlank())
            assertTrue(r.length <= 200)
        }
    }

    @Test
    fun `greeting replies vary by personality`() {
        val replies = Personality.entries.map { responder.localReply(it, "how are you") }
        assertTrue(replies.toSet().size >= 4) // personalities are distinguishable
    }
}
