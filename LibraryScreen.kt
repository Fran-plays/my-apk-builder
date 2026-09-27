package com.petmorph.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petmorph.ai.navigation.Routes
import com.petmorph.ai.ui.CompanionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavController, vm: CompanionViewModel) {
    val companions by vm.companions.collectAsState()
    var menuFor by remember { mutableStateOf<String?>(null) }
    var renameFor by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Companions") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
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
                Text("No companions yet")
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
                    Box {
                        CompanionCard(c, vm, onClick = { nav.navigate(Routes.preview(c.id)) })
                        IconButton(
                            onClick = { menuFor = c.id },
                            modifier = Modifier.align(Alignment.TopEnd),
                        ) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
                        DropdownMenu(
                            expanded = menuFor == c.id,
                            onDismissRequest = { menuFor = null },
                        ) {
                            DropdownMenuItem(text = { Text("Open") }, onClick = {
                                menuFor = null; nav.navigate(Routes.preview(c.id))
                            })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                menuFor = null; renameFor = c.id; renameText = c.name
                            })
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = {
                                menuFor = null; vm.duplicate(c.id)
                            })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                menuFor = null; vm.delete(c.id)
                            })
                        }
                    }
                }
            }
        }
    }

    renameFor?.let { id ->
        AlertDialog(
            onDismissRequest = { renameFor = null },
            title = { Text("Rename companion") },
            text = {
                OutlinedTextField(value = renameText, onValueChange = { renameText = it },
                    singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.rename(id, renameText.trim().ifBlank { "Companion" })
                    renameFor = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameFor = null }) { Text("Cancel") } },
        )
    }
}
