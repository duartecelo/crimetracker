package com.crimetracker.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.crimetracker.app.data.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    groups: List<Group>,
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit // groupId, content
) {
    var content by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<Group?>(groups.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Criar Publicação",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }

                // Group Selector
                Box {
                    OutlinedTextField(
                        value = selectedGroup?.nome ?: "Selecione um grupo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Publicar em") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Selecionar grupo")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.nome) },
                                onClick = {
                                    selectedGroup = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 1000) content = it },
                    label = { Text("O que está acontecendo?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10,
                    supportingText = { Text("${content.length}/1000") }
                )

                // Actions
                Button(
                    onClick = {
                        selectedGroup?.let { group ->
                            if (content.isNotBlank()) {
                                onPost(group.id, content)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() && selectedGroup != null
                ) {
                    Text("Publicar")
                }
            }
        }
    }
}
