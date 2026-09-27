package com.petmorph.ai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.data.model.CompanionEntity
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: CompanionViewModel) {
    val companions by vm.companions.collectAsState()
    val isPro by vm.container.subscription.isPro.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var showProLimit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Companions") },
                actions = {
                    IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        if (vm.container.subscription.canCreateCompanion(companions.size)) {
                            nav.navigate(Routes.CREATE)
                        } else showProLimit = true
                    }
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Create Companion") },
            )
        }
    ) { padding ->
        if (companions.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🐣", style = MaterialTheme.typography.displayLarge)
                Text("No companions yet", style = MaterialTheme.typography.headlineSmall)
                Text("Tap + to create your first companion!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(companions, key = { it.id }) { c ->
                    CompanionCard(c, vm, onClick = { nav.navigate(Routes.preview(c.id)) })
                }
            }
        }
    }

    if (showProLimit) {
        AlertDialog(
            onDismissRequest = { showProLimit = false },
            confirmButton = { TextButton(onClick = { showProLimit = false }) { Text("OK") } },
            title = { Text("Free plan limit") },
            text = { Text("The free plan includes 1 companion. Upgrade to Pro for unlimited companions, AI chat, and premium effects.") },
        )
    }
}

@Composable
fun CompanionCard(c: CompanionEntity, vm: CompanionViewModel, onClick: () -> Unit) {
    val bmp = remember(c.processedImagePath) { vm.loadBitmap(c.processedImagePath)?.asImageBitmap() }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (bmp != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bmp, contentDescription = c.name,
                        contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp))
                }
            }
            Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(c.type.name.lowercase().replace('_', ' '), style = MaterialTheme.typography.labelSmall)
        }
    }
}
