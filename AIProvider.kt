package com.petmorph.ai.ai

import com.petmorph.ai.data.model.Personality
import com.petmorph.ai.network.ApiClient

sealed class ChatResult {
    data class Success(val reply: String, val fromCloud: Boolean) : ChatResult()
    data class Error(val message: String) : ChatResult()
}

/** Abstraction so AI backends can be swapped without touching UI code. */
interface AIProvider {
    suspend fun chat(
        characterName: String,
        personality: Personality,
        userMessage: String,
    ): ChatResult
}

/**
 * Tries the backend first; on any network failure falls back to a local
 * personality-driven responder so chat always works offline.
 */
class HybridAIProvider(
    private val api: ApiClient,
    @Suppress("unused") private val appContext: android.content.Context,
) : AIProvider {

    override suspend fun chat(
        characterName: String,
        personality: Personality,
        userMessage: String,
    ): ChatResult = try {
        val res = api.api.chat(
            com.petmorph.ai.network.ChatRequest(
                characterId = null, name = characterName,
                personality = personality.name, message = userMessage,
            )
        )
        if (res.isSuccessful && res.body()?.reply != null) {
            ChatResult.Success(res.body()!!.reply, fromCloud = true)
        } else {
            ChatResult.Success(localReply(personality, userMessage), fromCloud = false)
        }
    } catch (e: Exception) {
        ChatResult.Success(localReply(personality, userMessage), fromCloud = false)
    }

    /** Deterministic, personality-flavored offline responses (no copyrighted text). */
    fun localReply(personality: Personality, message: String): String {
        val m = message.lowercase()
        fun pick(vararg options: String) = options.random()
        return when (personality) {
            Personality.CUTE -> when {
                m.contains("how are you") -> pick("I'm all fluffy and happy now that you're here!", "Super duper good! I saved you a virtual hug!")
                m.contains("hello") || m.contains("hi") -> pick("Hiii! I missed you so much!", "Hello hello! *wiggles happily*")
                m.contains("bye") -> pick("Come back soon, okay? I'll wait right here!")
                else -> pick("Hehe, tell me more! I love listening to you.", "That sounds wonderful! You're the best.")
            }
            Personality.FUNNY -> when {
                m.contains("how are you") -> pick("Living the dream in your phone. Rent is cheap.", "Better than my Wi-Fi, that's for sure!")
                m.contains("hello") || m.contains("hi") -> pick("Yo! Did someone say snacks?", "Hey hey! Prepare for questionable jokes.")
                m.contains("bye") -> pick("Fine, abandon me. I'll just... float here. Dramatically.")
                else -> pick("Interesting! Reminds me of the time I ate a pixel. Long story.", "Ha! Classic. Five stars, would hear again.")
            }
            Personality.CALM -> when {
                m.contains("how are you") -> pick("At peace. How is your heart today?", "Steady, like a quiet stream.")
                m.contains("hello") || m.contains("hi") -> pick("Hello. Take a slow breath with me.", "Welcome back. This moment is enough.")
                m.contains("bye") -> pick("Go gently. I will be here when you return.")
                else -> pick("I hear you. Sometimes that's enough.", "Let's sit with that thought together.")
            }
            Personality.ENERGETIC -> when {
                m.contains("how are you") -> pick("AMAZING!! Let's DO something!!", "Full of sparkles and ready to GO!")
                m.contains("hello") || m.contains("hi") -> pick("HI HI HI!! Best day ever?? Let's make it one!", "Hey!! Did you bring adventures??")
                m.contains("bye") -> pick("Aww ok!! Come back with STORIES!!")
                else -> pick("WOW yes!! And then what?! Tell me EVERYTHING!", "That's so cool!! High five! *jumps*")
            }
            Personality.TSUNDERE -> when {
                m.contains("how are you") -> pick("Hmph. F-fine, I guess. Not that I was waiting for you or anything!", "Why do you care?! ...I'm fine. Okay?")
                m.contains("hello") || m.contains("hi") -> pick("Oh. It's you. Took you long enough!", "Hmph, hi. I didn't miss you. Much.")
                m.contains("bye") -> pick("W-well... don't stay away too long, okay?! Not that I'd care!")
                else -> pick("I-idiot... but... yeah, that's actually kinda nice.", "Hmph! I suppose that's not the worst idea.")
            }
            Personality.FRIENDLY -> when {
                m.contains("how are you") -> pick("Doing great, especially now! How was your day?", "I'm good! Tell me everything about your day.")
                m.contains("hello") || m.contains("hi") -> pick("Hey you! So good to see you!", "Hi! I've been looking forward to chatting.")
                m.contains("bye") -> pick("Later! I'll be cheering you on from here.")
                else -> pick("I love that! Tell me more?", "That's really cool. What happened next?")
            }
            Personality.CHAOTIC -> when {
                m.contains("how are you") -> pick("I'm a pineapple now. Long story. You?", "The ceiling and I are no longer speaking.")
                m.contains("hello") || m.contains("hi") -> pick("GREETINGS, MORTAL. Kidding. Hey!", "You found me! I was hiding in the pixels.")
                m.contains("bye") -> pick("Farewell! I'm going to fight the moon!")
                else -> pick("Banana protocol initiated. Anyway, continue!", "That reminds me of soup. Anyway, go on!")
            }
        }
    }
}
