package com.petmorph.ai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.data.model.CompanionType
import com.petmorph.ai.data.model.Personality
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel
import com.petmorph.ai.ui.ProcessingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCompanionScreen(nav: NavController, vm: CompanionViewModel) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CompanionType.CUTE_PET) }
    var personality by remember { mutableStateOf(Personality.CUTE) }
    var picked by remember { mutableStateOf<Uri?>(null) }
    val processing by vm.processing.collectAsState()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        picked = uri
    }

    LaunchedEffect(processing) {
        if (processing is ProcessingState.Success) {
            val id = (processing as ProcessingState.Success).companionId
            nav.navigate(Routes.setup(id)) { popUpTo(Routes.CREATE) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Companion") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Step 1: name
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Companion name") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )

            // Step 2: type
            Text("Companion type", style = MaterialTheme.typography.titleMedium)
            Column {
                CompanionType.entries.forEach { t ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = type == t, onClick = { type = t })
                        Text(
                            t.name.lowercase().replace('_', ' ')
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }

            // Step 3: personality
            Text("Personality", style = MaterialTheme.typography.titleMedium)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(personality.label)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Personality.entries.forEach { p ->
                        DropdownMenuItem(text = { Text(p.label) }, onClick = {
                            personality = p; expanded = false
                        })
                    }
                }
            }

            // Step 4: image
            Text("Image", style = MaterialTheme.typography.titleMedium)
            if (picked == null) {
                OutlinedButton(
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("📷  Upload Image (PNG / JPG / WEBP)") }
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                        )
                        Text("Image selected ✓")
                        TextButton(onClick = { picker.launch("image/*") }) { Text("Choose another") }
                    }
                }
            }

            if (processing is ProcessingState.Error) {
                Text(
                    "⚠️ ${(processing as ProcessingState.Error).message}. Try again.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            when (processing) {
                ProcessingState.Loading -> {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Processing your character… detecting subject, removing background…")
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            picked?.let {
                                vm.createFromImage(it, name.ifBlank { "Companion" }, type, personality)
                            }
                        },
                        enabled = picked != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text("Create my companion!") }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
