package com.petmorph.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.animation.*
import com.petmorph.ai.data.model.CompanionEntity
import com.petmorph.ai.data.model.VoiceConfig
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel

/** Step after processing: meet the companion, tune voice, preview animations. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(nav: NavController, vm: CompanionViewModel, companionId: String) {
    var entity by remember { mutableStateOf<CompanionEntity?>(null) }
    var anim by remember { mutableStateOf(AnimationState.IDLE) }
    val talking by vm.talking.collectAsState()
    val animator = rememberCompanionAnimator()

    LaunchedEffect(companionId) { entity = vm.container.repository.get(companionId) }

    LaunchedEffect(anim) { animator.play(anim, talking) }

    val e = entity ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    var voice by remember(e) { mutableStateOf(e.voice()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meet ${e.name}!") }) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Meet your new companion!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            CompanionCanvas(
                animator = animator,
                bitmap = remember(e.processedImagePath) { vm.loadBitmap(e.processedImagePath) },
                sizeDp = 200.dp,
            )
            // Animation picker
            Text("Animations", style = MaterialTheme.typography.titleMedium)
            FlowRow {
                AnimationState.entries.forEach { s ->
                    FilterChip(
                        selected = anim == s,
                        onClick = { anim = s },
                        label = { Text(s.label) },
                        modifier = Modifier.padding(end = 6.dp, bottom = 6.dp),
                    )
                }
            }
            HorizontalDivider()
            Text("Voice (local TTS)", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Speed"); Spacer(Modifier.weight(1f))
                Text("%.1f".format(voice.rate))
            }
            Slider(value = voice.rate, onValueChange = { voice = voice.copy(rate = it) }, valueRange = 0.5f..2f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pitch"); Spacer(Modifier.weight(1f))
                Text("%.1f".format(voice.pitch))
            }
            Slider(value = voice.pitch, onValueChange = { voice = voice.copy(pitch = it) }, valueRange = 0.5f..2f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mute voice"); Spacer(Modifier.weight(1f))
                Switch(checked = voice.muted, onCheckedChange = { voice = voice.copy(muted = it) })
            }
            OutlinedButton(onClick = {
                vm.updateCompanion(e.copy(voiceJson = CompanionEntity.voiceToJson(voice)))
                vm.speak(e.copy(voiceJson = CompanionEntity.voiceToJson(voice)),
                    "Hi! I'm ${e.name}! Let's have fun together!")
            }, modifier = Modifier.fillMaxWidth()) { Text("▶ Test voice") }

            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { nav.popBackStack() }) { Text("Back") }
                Button(onClick = {
                    vm.updateCompanion(e.copy(voiceJson = CompanionEntity.voiceToJson(voice)))
                    nav.navigate(Routes.preview(e.id))
                }) { Text("Interact →") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow { content() }
}
