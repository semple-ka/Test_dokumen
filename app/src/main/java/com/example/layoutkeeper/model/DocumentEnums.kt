package com.example.layoutkeeper.model

enum class DocumentFormat(val displayName: String, val extension: String, val isEditable: Boolean) {
    PDF("Adobe PDF", "pdf", true),
    DOCX("Word Document", "docx", true),
    TXT("Plain Text", "txt", true),
    UNKNOWN("Unsupported", "", false);

    companion object {
        fun fromFileName(name: String): DocumentFormat {
            val ext = name.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "pdf" -> PDF
                "docx" -> DOCX
                "txt" -> TXT
                else -> UNKNOWN
            }
        }
    }
}

enum class PdfEditClassification(val label: String, val description: String) {
    TYPE_1_NATIVE("Type 1: Native Content Editing", "Modifies underlying PDF content stream directly."),
    TYPE_2_RECONSTRUCTION("Type 2: Content Reconstruction", "Reconstructs affected text block while preserving surrounding elements."),
    TYPE_3_OVERLAY("Type 3: Redaction & Matched Vector Overlay", "Removes visible target text and renders exact vector text in-place."),
    TYPE_4_OCR("Type 4: OCR-Based Image Editing", "Scanned page requiring optical character review and correction.");
}

enum class OverflowStrategy(val title: String, val description: String) {
    PRESERVE_SIZE_WARN("Option A: Preserve Size", "Keep original font size; warn if text exceeds bounding box."),
    ADJUST_WITHIN_LIMITS("Option B: Adjust Size (10-15%)", "Dynamically adjust font size within approved safe margin."),
    EXPAND_AREA("Option C: Expand Text Area", "Expand bounded container if adjacent space is unobstructed."),
    MANUAL_ADJUST("Option D: Manual Verification", "Inspect visually on canvas before committing change.");
}

enum class ElementContainerType {
    PARAGRAPH,
    TABLE_CELL,
    HEADER,
    FOOTER,
    TEXT_BOX,
    PDF_LINE
}
