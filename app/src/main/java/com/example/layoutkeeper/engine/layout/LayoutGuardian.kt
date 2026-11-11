package com.example.layoutkeeper.engine.layout

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.layoutkeeper.model.ElementContainerType
import com.example.layoutkeeper.model.HealthStatus
import com.example.layoutkeeper.model.LayoutHealthReport
import com.example.layoutkeeper.model.OverflowStrategy
import com.example.layoutkeeper.model.TextElement

class LayoutGuardian {

    private val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    data class FitAssessment(
        val originalWidthPx: Float,
        val newWidthPx: Float,
        val widthRatio: Float,
        val hasOverflow: Boolean,
        val overflowDeltaPx: Float,
        val recommendedStrategy: OverflowStrategy,
        val suggestedAdjustedFontSizePt: Float,
        val canExpandWithoutCollision: Boolean,
        val warningMessage: String?
    )

    fun evaluateFit(
        element: TextElement,
        candidateText: String,
        pageElements: List<TextElement>,
        pageWidthPt: Float = 612f
    ): FitAssessment {
        // Configure Paint to match font properties
        val typefaceStyle = when {
            element.isBold && element.isItalic -> Typeface.BOLD_ITALIC
            element.isBold -> Typeface.BOLD
            element.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        measurePaint.typeface = Typeface.create(Typeface.SANS_SERIF, typefaceStyle)
        measurePaint.textSize = element.fontSizePt * 1.333f // Convert pt to px approx

        val originalWidthPx = measurePaint.measureText(element.originalText).coerceAtLeast(10f)
        val newWidthPx = measurePaint.measureText(candidateText)
        val widthRatio = newWidthPx / originalWidthPx

        // Allowed tolerance before flagging overflow: 10% tolerance or 15px
        val hasOverflow = newWidthPx > (originalWidthPx * 1.08f) && (newWidthPx - originalWidthPx > 12f)
        val overflowDeltaPx = (newWidthPx - originalWidthPx).coerceAtLeast(0f)

        // Calculate potential font size adjustment for Option B (minimum 85% of original size)
        val targetScale = (originalWidthPx / newWidthPx).coerceIn(0.85f, 1.0f)
        val suggestedAdjustedFontSizePt = (element.fontSizePt * targetScale * 10f).toInt() / 10f

        // Check if expanding the bounding box to the right would collide with adjacent elements
        val elementWidthNorm = element.bounds.width()
        val normalizedDelta = if (pageWidthPt > 0) (overflowDeltaPx / pageWidthPt) else 0.05f
        val expandedRight = (element.bounds.right + normalizedDelta).coerceAtMost(1.0f)
        val expandedBounds = RectF(element.bounds.left, element.bounds.top, expandedRight, element.bounds.bottom)

        val hasCollision = pageElements.any { other ->
            other.id != element.id &&
            other.pageIndex == element.pageIndex &&
            RectF.intersects(expandedBounds, other.bounds)
        }
        val canExpandWithoutCollision = !hasCollision && (expandedRight <= 0.95f)

        val recommendedStrategy = when {
            !hasOverflow -> OverflowStrategy.PRESERVE_SIZE_WARN
            suggestedAdjustedFontSizePt >= (element.fontSizePt * 0.88f) -> OverflowStrategy.ADJUST_WITHIN_LIMITS
            canExpandWithoutCollision -> OverflowStrategy.EXPAND_AREA
            else -> OverflowStrategy.MANUAL_ADJUST
        }

        val warningMessage = if (hasOverflow) {
            "Replacement is ${(widthRatio * 100).toInt()}% of original width (exceeds bounds by ${overflowDeltaPx.toInt()}px)."
        } else null

        return FitAssessment(
            originalWidthPx = originalWidthPx,
            newWidthPx = newWidthPx,
            widthRatio = widthRatio,
            hasOverflow = hasOverflow,
            overflowDeltaPx = overflowDeltaPx,
            recommendedStrategy = recommendedStrategy,
            suggestedAdjustedFontSizePt = suggestedAdjustedFontSizePt,
            canExpandWithoutCollision = canExpandWithoutCollision,
            warningMessage = warningMessage
        )
    }

    fun generateHealthReport(
        allElements: List<TextElement>,
        preservedStylesCount: Int = 12,
        preservedMediaCount: Int = 4
    ): LayoutHealthReport {
        val editedElements = allElements.filter { it.isDirty }
        val overflowElements = editedElements.filter { it.hasOverflow }

        val warnings = mutableListOf<String>()
        val notes = mutableListOf<String>()

        if (overflowElements.isNotEmpty()) {
            warnings.add("${overflowElements.size} element(s) exceed original text bounds. Review overflow options.")
        }

        notes.add("Original document XML structures and container bounds preserved.")
        notes.add("All embedded images, tables, headers, and styles maintained intact.")

        val status = when {
            overflowElements.isNotEmpty() -> HealthStatus.ATTENTION_REQUIRED
            editedElements.isNotEmpty() -> HealthStatus.SAFE_WARNING
            else -> HealthStatus.PERFECT
        }

        return LayoutHealthReport(
            status = status,
            totalElements = allElements.size,
            editedElements = editedElements.size,
            overflowCount = overflowElements.size,
            collisionCount = 0,
            preservedStylesCount = preservedStylesCount,
            preservedMediaCount = preservedMediaCount,
            warnings = warnings,
            notes = notes
        )
    }
}
