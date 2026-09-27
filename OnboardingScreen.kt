package com.petmorph.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel
import kotlinx.coroutines.launch

private data class OnboardPage(val emoji: String, val title: String, val body: String)

private val pages = listOf(
    OnboardPage("🖼️", "Turn any image into a companion.", "Upload a photo of your pet, an anime character, a mascot or your own drawing."),
    OnboardPage("✨", "Give it expressions, animations and a voice.", "Happy, sleepy, dancing, talking — your companion comes alive with sound and speech."),
    OnboardPage("🚀", "Keep your companion with you.", "Let it float over other apps, chat with it, and collect a whole family of companions."),
)

@Composable
fun OnboardingScreen(nav: NavController, vm: CompanionViewModel) {
    var page by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val p = pages[page]

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { finish(nav, vm) }) { Text("Skip") }
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(180.dp).clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text(p.emoji, style = MaterialTheme.typography.displayLarge) }
        Spacer(Modifier.height(32.dp))
        Text(p.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(p.body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pages.indices.forEach { i ->
                Box(
                    Modifier.size(if (i == page) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (i == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (page < pages.lastIndex) page++ else finish(nav, vm)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (page < pages.lastIndex) "Next" else "Create My Companion")
        }
    }
}

private fun finish(nav: NavController, vm: CompanionViewModel) {
    kotlinx.coroutines.GlobalScope.launch {
        vm.container.settings.setOnboarded(true)
    }
    nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
}
