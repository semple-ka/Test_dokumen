package com.example.layoutkeeper.model

import android.graphics.RectF

data class TextElement(
    val id: String,
    val pageIndex: Int,
    val originalText: String,
    var currentText: String,
    val bounds: RectF, // Normalized coordinates (0f..1f) relative to page dimensions
    val fontFamily: String = "SansSerif",
    val fontSizePt: Float = 12f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val colorHex: String = "#111827",
    val alignment: String = "LEFT",
    val containerType: ElementContainerType = ElementContainerType.PARAGRAPH,
    val sectionIndex: Int = 0,
    val runIndex: Int = 0,
    val tableRowIndex: Int = -1,
    val tableColIndex: Int = -1,
    var isModified: Boolean = false,
    var customFontSizePt: Float = fontSizePt,
    var overflowDeltaPx: Float = 0f,
    var hasOverflow: Boolean = false
) {
    val isDirty: Boolean
        get() = currentText != originalText

    fun cloneWithNewText(newText: String, newCustomFontSizePt: Float = fontSizePt): TextElement {
        return this.copy(
            currentText = newText,
            isModified = newText != originalText,
            customFontSizePt = newCustomFontSizePt
        )
    }
}

data class EditOperation(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val elementId: String,
    val pageIndex: Int,
    val previousText: String,
    val newText: String,
    val classification: PdfEditClassification,
    val strategy: OverflowStrategy,
    val overflowDeltaPx: Float = 0f,
    val previousFontSizePt: Float,
    val newFontSizePt: Float
)
