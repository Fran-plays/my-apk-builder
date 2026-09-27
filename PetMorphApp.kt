package com.petmorph.ai

import android.app.Application
import com.petmorph.ai.core.AppContainer

class PetMorphApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.sounds // eager init so ToneGenerator is ready
    }
}
