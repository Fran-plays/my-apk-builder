package com.petmorph.ai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.petmorph.ai.navigation.AppNavHost
import com.petmorph.ai.ui.theme.PetMorphTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as PetMorphApp
            val theme by app.container.settings.themeMode.collectAsState(initial = "system")
            val dark = when (theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            PetMorphTheme(darkTheme = dark) {
                AskNotificationPermissionOnce()
                AppNavHost()
            }
        }
    }

    @Composable
    private fun AskNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < 33) return
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        val scope = rememberCoroutineScope()
        val asked = androidx.compose.runtime.remember { kotlinx.coroutines.runBlocking {
            (application as PetMorphApp).container.settings.isOnboarded
        } }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
