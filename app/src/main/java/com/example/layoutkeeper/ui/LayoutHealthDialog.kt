package com.example.layoutkeeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.layoutkeeper.model.HealthStatus
import com.example.layoutkeeper.model.LayoutHealthReport
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess

@Composable
fun LayoutHealthDialog(
    report: LayoutHealthReport?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (report?.status) {
                        HealthStatus.PERFECT -> Icons.Default.CheckCircle
                        HealthStatus.SAFE_WARNING -> Icons.Default.Info
                        HealthStatus.ATTENTION_REQUIRED, null -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when (report?.status) {
                        HealthStatus.PERFECT -> EmeraldSuccess
                        HealthStatus.SAFE_WARNING -> MaterialTheme.colorScheme.primary
                        HealthStatus.ATTENTION_REQUIRED, null -> AmberWarning
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Layout Integrity Audit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (report != null) {
                    StatusBadge(report.status)
                    Spacer(modifier = Modifier.height(12.dp))

                    MetricRow("Total Document Elements:", "${report.totalElements}")
                    MetricRow("Edited Elements:", "${report.editedElements}")
                    MetricRow("Preserved Styles:", "${report.preservedStylesCount}")
                    MetricRow("Preserved Media / Tables:", "${report.preservedMediaCount}")
                    MetricRow("Overflow Warnings:", "${report.overflowCount}", isAlert = report.overflowCount > 0)
                    MetricRow("Bounding Collisions:", "${report.collisionCount}", isAlert = report.collisionCount > 0)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (report.warnings.isNotEmpty()) {
                        Text(
                            text = "WARNINGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberWarning,
                            fontWeight = FontWeight.Bold
                        )
                        report.warnings.forEach { w ->
                            Text(
                                text = "• $w",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DOCUMENT INTEGRITY GUARANTEE",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    report.notes.forEach { n ->
                        Text(
                            text = "✓ $n",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                } else {
                    Text("No layout audit data available.")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Audit")
            }
        }
    )
}

@Composable
private fun StatusBadge(status: HealthStatus) {
    val bg = when (status) {
        HealthStatus.PERFECT -> EmeraldSuccess.copy(alpha = 0.15f)
        HealthStatus.SAFE_WARNING -> MaterialTheme.colorScheme.primaryContainer
        HealthStatus.ATTENTION_REQUIRED -> AmberWarning.copy(alpha = 0.15f)
    }
    val fg = when (status) {
        HealthStatus.PERFECT -> EmeraldSuccess
        HealthStatus.SAFE_WARNING -> MaterialTheme.colorScheme.onPrimaryContainer
        HealthStatus.ATTENTION_REQUIRED -> AmberWarning
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status.label,
            color = fg,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String, isAlert: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) AmberWarning else MaterialTheme.colorScheme.onSurface
        )
    }
}
