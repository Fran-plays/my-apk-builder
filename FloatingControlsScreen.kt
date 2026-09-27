package com.petmorph.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.overlay.FloatingCompanionService
import com.petmorph.ai.overlay.OverlayPermissionHelper
import com.petmorph.ai.ui.CompanionViewModel

/** Controls for the floating overlay companion. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingControlsScreen(nav: NavController, vm: CompanionViewModel, companionId: String) {
    val opacity by vm.container.settings.overlayOpacityFlow.collectAsState(initial = 1f)
    val size by vm.container.settings.overlaySizeFlow.collectAsState(initial = 1f)
    val hasPermission = remember { OverlayPermissionHelper.hasPermission(nav.context) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Floating Companion") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!hasPermission) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Overlay permission not granted",
                            style = MaterialTheme.typography.titleMedium)
                        Text(OverlayPermissionHelper.explainBeforeRequest())
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            OverlayPermissionHelper.requestPermission(
                                nav.context as android.app.Activity)
                        }) { Text("Open permission settings") }
                    }
                }
            }

            Text("Opacity"); Text("%.0f%%".format(opacity * 100))
            Slider(value = opacity, onValueChange = {
                scope.launch { vm.container.settings.setOverlayOpacity(it) }
            })
            Text("Size"); Text("%.0f%%".format(size * 100))
            Slider(value = size, onValueChange = {
                scope.launch { vm.container.settings.setOverlaySize(it) }
            }, valueRange = 0.5f..2.5f)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    FloatingCompanionService.start(nav.context, companionId)
                }, enabled = hasPermission) { Text("Show companion") }
                OutlinedButton(onClick = {
                    FloatingCompanionService.stop(nav.context)
                }) { Text("Hide") }
            }
            Text(
                "Tips: drag to move • tap for a reaction • double-tap to open the app",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
