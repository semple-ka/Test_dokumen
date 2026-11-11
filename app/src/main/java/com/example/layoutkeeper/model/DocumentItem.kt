package com.example.layoutkeeper.model

import java.io.File

data class DocumentItem(
    val id: String,
    val title: String,
    val format: DocumentFormat,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val lastModified: Long,
    val isSample: Boolean = false,
    val originalFilePath: String,
    val workingFilePath: String,
    val hasUnsavedChanges: Boolean = false,
    val editCount: Int = 0,
    val isScanned: Boolean = false
) {
    val fileSizeFormatted: String
        get() {
            return when {
                fileSizeBytes >= 1024 * 1024 -> String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
                fileSizeBytes >= 1024 -> String.format("%.1f KB", fileSizeBytes / 1024.0)
                else -> "$fileSizeBytes B"
            }
        }

    val workingFile: File
        get() = File(workingFilePath)

    val originalFile: File
        get() = File(originalFilePath)
}

enum class HealthStatus(val label: String) {
    PERFECT("Optimal Layout"),
    SAFE_WARNING("Minor Text Shift"),
    ATTENTION_REQUIRED("Overflow Detected")
}

data class LayoutHealthReport(
    val status: HealthStatus,
    val totalElements: Int,
    val editedElements: Int,
    val overflowCount: Int,
    val collisionCount: Int,
    val preservedStylesCount: Int,
    val preservedMediaCount: Int,
    val warnings: List<String> = emptyList(),
    val notes: List<String> = emptyList()
) {
    val isReadyForExport: Boolean
        get() = collisionCount == 0 && (status != HealthStatus.ATTENTION_REQUIRED || overflowCount == 0)
}
