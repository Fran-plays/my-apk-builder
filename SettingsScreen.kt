package com.petmorph.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.ui.CompanionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController, vm: CompanionViewModel) {
    val theme by vm.container.settings.themeMode.collectAsState(initial = "system")
    val opacity by vm.container.settings.overlayOpacityFlow.collectAsState(initial = 1f)
    val size by vm.container.settings.overlaySizeFlow.collectAsState(initial = 1f)
    val soundsOn by vm.container.settings.soundsOnFlow.collectAsState(initial = true)
    val isPro by vm.container.subscription.isPro.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Theme"); Spacer(Modifier.weight(1f))
                SegmentedButtons(
                    options = listOf("system" to "System", "light" to "Light", "dark" to "Dark"),
                    selected = theme,
                ) { scope.launch { vm.container.settings.setThemeMode(it) } }
            }

            HorizontalDivider()
            Text("Floating companion", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Opacity"); Spacer(Modifier.weight(1f)); Text("%.0f%%".format(opacity * 100))
            }
            Slider(value = opacity, onValueChange = {
                scope.launch { vm.container.settings.setOverlayOpacity(it) }
            })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Size"); Spacer(Modifier.weight(1f)); Text("%.0f%%".format(size * 100))
            }
            Slider(value = size, onValueChange = {
                scope.launch { vm.container.settings.setOverlaySize(it) }
            }, valueRange = 0.5f..2.5f)

            HorizontalDivider()
            Text("Audio", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sound effects"); Spacer(Modifier.weight(1f))
                Switch(checked = soundsOn, onCheckedChange = {
                    scope.launch { vm.container.settings.setSoundsOn(it) }
                    vm.container.sounds.enabled = it
                })
            }

            HorizontalDivider()
            Text("Subscription", style = MaterialTheme.typography.titleMedium)
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isPro) "⭐ Pro plan active" else "Free plan",
                        style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isPro) "Unlimited companions, cloud AI chat, cloud TTS and premium effects."
                        else "1 companion · basic animations · local TTS. Pro adds unlimited companions, AI chat and cloud TTS."
                    )
                    if (!isPro) {
                        OutlinedButton(onClick = {
                            // Hook for Google Play Billing via SubscriptionService.billingDelegate.
                            // Until configured, unlock locally for development:
                            scope.launch { vm.container.subscription.enableProForDebug() }
                        }) { Text("Restore / Upgrade (via Play Billing)") }
                    }
                }
            }

            HorizontalDivider()
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text("PetMorph AI v1.0.0 — turn any image into your personal companion. " +
                "Offline features work without internet; AI chat & cloud TTS require the backend.")
        }
    }
}

@Composable
private fun SegmentedButtons(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row {
        options.forEach { (id, label) ->
            FilterChip(
                selected = selected == id,
                onClick = { onSelect(id) },
                label = { Text(label) },
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
