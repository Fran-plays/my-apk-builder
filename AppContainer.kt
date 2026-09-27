package com.petmorph.ai.core

import android.content.Context
import com.petmorph.ai.ai.AIProvider
import com.petmorph.ai.ai.HybridAIProvider
import com.petmorph.ai.audio.SoundEffectController
import com.petmorph.ai.data.local.CompanionDatabase
import com.petmorph.ai.data.repository.CompanionRepository
import com.petmorph.ai.image.ImageProcessor
import com.petmorph.ai.image.LocalImageProcessor
import com.petmorph.ai.network.ApiClient
import com.petmorph.ai.settings.SettingsManager
import com.petmorph.ai.storage.LocalStorageProvider
import com.petmorph.ai.storage.StorageProvider
import com.petmorph.ai.subscription.SubscriptionService
import com.petmorph.ai.tts.TTSController

/**
 * Manual dependency injection container (no framework needed, easy to test).
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val settings by lazy { SettingsManager(appContext) }
    val database by lazy { CompanionDatabase.get(appContext) }
    val repository by lazy { CompanionRepository(database.companionDao(), appContext) }

    val imageProcessor: ImageProcessor by lazy { LocalImageProcessor() }
    val storage: StorageProvider by lazy { LocalStorageProvider(appContext) }

    val api by lazy { ApiClient(BuildConfig.API_BASE_URL) }
    val ai: AIProvider by lazy { HybridAIProvider(api, appContext) }

    val tts by lazy { TTSController(appContext) }
    val sounds by lazy { SoundEffectController(appContext) }

    val subscription by lazy { SubscriptionService(settings) }
}
