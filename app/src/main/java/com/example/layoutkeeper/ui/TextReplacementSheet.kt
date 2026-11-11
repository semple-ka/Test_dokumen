package com.example.layoutkeeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.layoutkeeper.engine.layout.LayoutGuardian
import com.example.layoutkeeper.model.OverflowStrategy
import com.example.layoutkeeper.model.TextElement
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextReplacementSheet(
    element: TextElement,
    pageElements: List<TextElement>,
    onDismiss: () -> Unit,
    onApplyEdit: (elementId: String, newText: String, newFontSizePt: Float?, strategy: OverflowStrategy) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var textInput by remember { mutableStateOf(element.currentText) }
    var selectedStrategy by remember { mutableStateOf(OverflowStrategy.PRESERVE_SIZE_WARN) }
    var customFontSize by remember { mutableFloatStateOf(element.customFontSizePt) }

    val layoutGuardian = remember { LayoutGuardian() }
    val fitAssessment = remember(textInput) {
        layoutGuardian.evaluateFit(element, textInput, pageElements)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = element.containerType.name.replace('_', ' '),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${element.fontSizePt.toInt()}pt • ${element.fontFamily}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Intelligent Text Replacement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close sheet")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Original Text Reference
            Text(
                text = "ORIGINAL CONTENT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = element.originalText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Replacement Input
            Text(
                text = "REPLACEMENT TEXT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("replacement_text_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 4
            )

            // Live Character & Width Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Width Ratio: ${(fitAssessment.widthRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (fitAssessment.hasOverflow) AmberWarning else EmeraldSuccess,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${textInput.length} chars (Delta: ${textInput.length - element.originalText.length})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { (fitAssessment.widthRatio / 1.5f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (fitAssessment.hasOverflow) AmberWarning else EmeraldSuccess,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Overflow Warning Indicator Pill
            if (fitAssessment.hasOverflow) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fitAssessment.warningMessage ?: "Potential layout shift detected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overflow & Layout Preservation Strategy Options
            Text(
                text = "LAYOUT PRESERVATION STRATEGY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                StrategyOptionCard(
                    strategy = OverflowStrategy.PRESERVE_SIZE_WARN,
                    selected = selectedStrategy == OverflowStrategy.PRESERVE_SIZE_WARN,
                    onSelect = { selectedStrategy = OverflowStrategy.PRESERVE_SIZE_WARN }
                )
                StrategyOptionCard(
                    strategy = OverflowStrategy.ADJUST_WITHIN_LIMITS,
                    selected = selectedStrategy == OverflowStrategy.ADJUST_WITHIN_LIMITS,
                    onSelect = {
                        selectedStrategy = OverflowStrategy.ADJUST_WITHIN_LIMITS
                        customFontSize = fitAssessment.suggestedAdjustedFontSizePt
                    },
                    extraNote = "Suggested: ${fitAssessment.suggestedAdjustedFontSizePt}pt"
                )
                StrategyOptionCard(
                    strategy = OverflowStrategy.EXPAND_AREA,
                    selected = selectedStrategy == OverflowStrategy.EXPAND_AREA,
                    onSelect = { selectedStrategy = OverflowStrategy.EXPAND_AREA },
                    extraNote = if (fitAssessment.canExpandWithoutCollision) "Right clearance available" else "Collision risk detected"
                )
                StrategyOptionCard(
                    strategy = OverflowStrategy.MANUAL_ADJUST,
                    selected = selectedStrategy == OverflowStrategy.MANUAL_ADJUST,
                    onSelect = { selectedStrategy = OverflowStrategy.MANUAL_ADJUST }
                )
            }

            // Manual font size slider if Option B or Manual is selected
            if (selectedStrategy == OverflowStrategy.ADJUST_WITHIN_LIMITS || selectedStrategy == OverflowStrategy.MANUAL_ADJUST) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fine-tune Font Size",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.1f", customFontSize)}pt",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = customFontSize,
                    onValueChange = { customFontSize = (it * 10f).toInt() / 10f },
                    valueRange = (element.fontSizePt * 0.7f)..(element.fontSizePt * 1.3f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val finalFontSize = if (selectedStrategy == OverflowStrategy.ADJUST_WITHIN_LIMITS || selectedStrategy == OverflowStrategy.MANUAL_ADJUST) {
                            customFontSize
                        } else {
                            element.fontSizePt
                        }
                        onApplyEdit(element.id, textInput, finalFontSize, selectedStrategy)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("commit_edit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Edit")
                }
            }
        }
    }
}

@Composable
private fun StrategyOptionCard(
    strategy: OverflowStrategy,
    selected: Boolean,
    onSelect: () -> Unit,
    extraNote: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strategy.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strategy.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (extraNote != null) {
                    Text(
                        text = extraNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
