package com.crimetracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crimetracker.app.data.model.Report

@Composable
fun ReportFeedbackSection(
    report: Report,
    onUsefulClick: () -> Unit = {},
    onNotUsefulClick: () -> Unit = {},
    onReportAbuse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botões de feedback
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botão "Útil"
            OutlinedButton(
                onClick = onUsefulClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (report.userFeedback == "useful") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${report.usefulCount}")
            }
            
            // Botão "Não útil"
            OutlinedButton(
                onClick = onNotUsefulClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (report.userFeedback == "not_useful") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            ) {
                Icon(
                    Icons.Default.ThumbDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${report.notUsefulCount}")
            }
        }
        
        // Indicador de confiabilidade
        report.reliabilityScore?.let { score ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confiabilidade:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = when {
                        score >= 80f -> Color(0xFF10B981) // Verde
                        score >= 50f -> Color(0xFFF59E0B) // Laranja
                        else -> Color(0xFFEF4444) // Vermelho
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${score.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Botão de reportar abuso
        TextButton(
            onClick = onReportAbuse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reportar abuso")
        }
    }
}

@Composable
fun ReportAbuseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var showDescription by remember { mutableStateOf(false) }
    
    val reasons = listOf(
        "Conteúdo falso" to "false_content",
        "Conteúdo ofensivo" to "offensive",
        "Spam" to "spam",
        "Outro" to "other"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Abuso") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Motivo:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                reasons.forEach { (label, value) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedReason == value,
                            onClick = {
                                selectedReason = value
                                showDescription = value == "other"
                            }
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                if (showDescription) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${description.length}/200") },
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedReason?.let { reason ->
                        onConfirm(reason, if (showDescription) description else null)
                    }
                },
                enabled = selectedReason != null && (!showDescription || description.isNotBlank())
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

