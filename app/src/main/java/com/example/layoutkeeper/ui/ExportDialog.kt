package com.example.layoutkeeper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.layoutkeeper.model.DocumentItem

@Composable
fun ExportDialog(
    document: DocumentItem?,
    onDismiss: () -> Unit,
    onExport: (fileName: String) -> Unit
) {
    val defaultName = remember(document) {
        val base = document?.title?.replace(' ', '_') ?: "document"
        val ext = document?.format?.extension ?: "docx"
        "${base}_edited.$ext"
    }
    var fileName by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "The edited document will be exported as a new file. The original source document remains 100% untouched.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Export File Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_filename_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(fileName) },
                enabled = fileName.isNotBlank(),
                modifier = Modifier.testTag("confirm_export_button")
            ) {
                Text("Export & Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
