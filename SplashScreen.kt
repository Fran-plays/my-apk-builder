package com.petmorph.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(nav: NavController, vm: CompanionViewModel) {
    val onboarded by vm.container.settings.isOnboarded.collectAsState(initial = false)
    LaunchedEffect(onboarded) {
        delay(900)
        if (onboarded) nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
        else nav.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🐾", style = MaterialTheme.typography.displayLarge)
            Text(
                "PetMorph AI", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
            )
            Text("Turn any image into your companion", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
