package com.example.layoutkeeper.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.layoutkeeper.engine.docx.DocxEngine
import com.example.layoutkeeper.engine.layout.LayoutGuardian
import com.example.layoutkeeper.engine.pdf.PdfEngine
import com.example.layoutkeeper.model.DocumentFormat
import com.example.layoutkeeper.model.DocumentItem
import com.example.layoutkeeper.model.EditOperation
import com.example.layoutkeeper.model.LayoutHealthReport
import com.example.layoutkeeper.model.PdfEditClassification
import com.example.layoutkeeper.model.TextElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocumentRepository(
    private val context: Context,
    private val docxEngine: DocxEngine = DocxEngine(),
    private val pdfEngine: PdfEngine = PdfEngine(context),
    private val layoutGuardian: LayoutGuardian = LayoutGuardian()
) {
    private val sandboxDir: File = File(context.filesDir, "sandbox").apply { mkdirs() }
    private val samplesDir: File = File(context.filesDir, "samples").apply { mkdirs() }

    private val _documents = MutableStateFlow<List<DocumentItem>>(emptyList())
    val documents: StateFlow<List<DocumentItem>> = _documents.asStateFlow()

    private val _activeDocument = MutableStateFlow<DocumentItem?>(null)
    val activeDocument: StateFlow<DocumentItem?> = _activeDocument.asStateFlow()

    private val _activeElements = MutableStateFlow<List<TextElement>>(emptyList())
    val activeElements: StateFlow<List<TextElement>> = _activeElements.asStateFlow()

    private val _undoStack = MutableStateFlow<List<EditOperation>>(emptyList())
    val undoStack: StateFlow<List<EditOperation>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditOperation>>(emptyList())
    val redoStack: StateFlow<List<EditOperation>> = _redoStack.asStateFlow()

    private val _healthReport = MutableStateFlow<LayoutHealthReport?>(null)
    val healthReport: StateFlow<LayoutHealthReport?> = _healthReport.asStateFlow()

    suspend fun initializeSamplesIfNeeded() = withContext(Dispatchers.IO) {
        val sampleDocx = File(samplesDir, "Executive_Consulting_Agreement.docx")
        val samplePdf = File(samplesDir, "Quarterly_Performance_Invoice.pdf")
        val sampleScannedPdf = File(samplesDir, "Scanned_Delivery_Receipt.pdf")

        if (!sampleDocx.exists()) {
            createSampleDocxFile(sampleDocx)
        }
        if (!samplePdf.exists()) {
            createSamplePdfFile(samplePdf, isScanned = false)
        }
        if (!sampleScannedPdf.exists()) {
            createSamplePdfFile(sampleScannedPdf, isScanned = true)
        }

        val items = mutableListOf<DocumentItem>()
        if (sampleDocx.exists()) {
            items.add(createDocumentItemFromFile(sampleDocx, isSample = true))
        }
        if (samplePdf.exists()) {
            items.add(createDocumentItemFromFile(samplePdf, isSample = true, isScanned = false))
        }
        if (sampleScannedPdf.exists()) {
            items.add(createDocumentItemFromFile(sampleScannedPdf, isSample = true, isScanned = true))
        }

        _documents.value = items
    }

    private fun createDocumentItemFromFile(
        sourceFile: File,
        isSample: Boolean = false,
        isScanned: Boolean = false
    ): DocumentItem {
        val id = UUID.randomUUID().toString()
        val format = DocumentFormat.fromFileName(sourceFile.name)
        val workingFile = File(sandboxDir, "working_${id}_${sourceFile.name}")
        sourceFile.copyTo(workingFile, overwrite = true)

        val pageCount = when (format) {
            DocumentFormat.DOCX -> 2
            DocumentFormat.PDF -> 1
            else -> 1
        }

        return DocumentItem(
            id = id,
            title = sourceFile.nameWithoutExtension.replace('_', ' '),
            format = format,
            fileSizeBytes = sourceFile.length(),
            pageCount = pageCount,
            lastModified = sourceFile.lastModified(),
            isSample = isSample,
            originalFilePath = sourceFile.absolutePath,
            workingFilePath = workingFile.absolutePath,
            isScanned = isScanned
        )
    }

    suspend fun openDocument(item: DocumentItem) = withContext(Dispatchers.IO) {
        _activeDocument.value = item
        _undoStack.value = emptyList()
        _redoStack.value = emptyList()

        when (item.format) {
            DocumentFormat.DOCX -> {
                val parseResult = docxEngine.parseDocx(item.workingFile)
                _activeElements.value = parseResult.elements
                _healthReport.value = layoutGuardian.generateHealthReport(parseResult.elements)
            }
            DocumentFormat.PDF -> {
                val inspection = pdfEngine.inspectAndParsePdf(item.workingFile)
                _activeElements.value = inspection.elements
                _healthReport.value = layoutGuardian.generateHealthReport(inspection.elements)
            }
            DocumentFormat.TXT -> {
                val lines = item.workingFile.readLines()
                val elements = lines.mapIndexed { idx, line ->
                    TextElement(
                        id = "txt_line_$idx",
                        pageIndex = 0,
                        originalText = line,
                        currentText = line,
                        bounds = android.graphics.RectF(0.1f, 0.08f + (idx * 0.04f), 0.9f, 0.11f + (idx * 0.04f)),
                        fontSizePt = 12f
                    )
                }
                _activeElements.value = elements
                _healthReport.value = layoutGuardian.generateHealthReport(elements)
            }
            DocumentFormat.UNKNOWN -> {
                _activeElements.value = emptyList()
            }
        }
    }

    suspend fun importFromUri(uri: Uri, displayName: String): DocumentItem = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val cleanName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val originalFile = File(sandboxDir, "orig_${id}_$cleanName")
        val workingFile = File(sandboxDir, "working_${id}_$cleanName")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        }
        originalFile.copyTo(workingFile, overwrite = true)

        val format = DocumentFormat.fromFileName(cleanName)
        val newItem = DocumentItem(
            id = id,
            title = cleanName.substringBeforeLast('.').replace('_', ' '),
            format = format,
            fileSizeBytes = originalFile.length(),
            pageCount = 1,
            lastModified = System.currentTimeMillis(),
            isSample = false,
            originalFilePath = originalFile.absolutePath,
            workingFilePath = workingFile.absolutePath,
            isScanned = cleanName.contains("scanned", ignoreCase = true)
        )

        _documents.value = listOf(newItem) + _documents.value
        newItem
    }

    suspend fun applyEdit(
        elementId: String,
        newText: String,
        newFontSizePt: Float? = null,
        strategy: com.example.layoutkeeper.model.OverflowStrategy = com.example.layoutkeeper.model.OverflowStrategy.PRESERVE_SIZE_WARN
    ) = withContext(Dispatchers.Default) {
        val currentList = _activeElements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == elementId }
        if (index == -1) return@withContext

        val target = currentList[index]
        val oldText = target.currentText
        val oldSize = target.customFontSizePt
        val updatedSize = newFontSizePt ?: target.fontSizePt

        // Evaluate fit and overflow
        val fit = layoutGuardian.evaluateFit(target, newText, currentList)

        val updated = target.cloneWithNewText(newText, updatedSize).apply {
            this.hasOverflow = fit.hasOverflow
            this.overflowDeltaPx = fit.overflowDeltaPx
        }
        currentList[index] = updated
        _activeElements.value = currentList

        // Track operation in undo stack
        val activeDoc = _activeDocument.value
        val classification = if (activeDoc?.format == DocumentFormat.PDF) {
            if (activeDoc.isScanned) PdfEditClassification.TYPE_4_OCR else PdfEditClassification.TYPE_3_OVERLAY
        } else {
            PdfEditClassification.TYPE_1_NATIVE
        }

        val op = EditOperation(
            id = UUID.randomUUID().toString(),
            elementId = elementId,
            pageIndex = target.pageIndex,
            previousText = oldText,
            newText = newText,
            classification = classification,
            strategy = strategy,
            overflowDeltaPx = fit.overflowDeltaPx,
            previousFontSizePt = oldSize,
            newFontSizePt = updatedSize
        )
        _undoStack.value = listOf(op) + _undoStack.value
        _redoStack.value = emptyList()

        _activeDocument.value = _activeDocument.value?.copy(
            hasUnsavedChanges = true,
            editCount = (_activeDocument.value?.editCount ?: 0) + 1
        )

        _healthReport.value = layoutGuardian.generateHealthReport(currentList)
    }

    suspend fun undo() = withContext(Dispatchers.Default) {
        val undoList = _undoStack.value
        if (undoList.isEmpty()) return@withContext

        val op = undoList.first()
        _undoStack.value = undoList.drop(1)

        val currentList = _activeElements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == op.elementId }
        if (index != -1) {
            val target = currentList[index]
            currentList[index] = target.cloneWithNewText(op.previousText, op.previousFontSizePt)
            _activeElements.value = currentList
        }

        _redoStack.value = listOf(op) + _redoStack.value
        _healthReport.value = layoutGuardian.generateHealthReport(currentList)
    }

    suspend fun redo() = withContext(Dispatchers.Default) {
        val redoList = _redoStack.value
        if (redoList.isEmpty()) return@withContext

        val op = redoList.first()
        _redoStack.value = redoList.drop(1)

        val currentList = _activeElements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == op.elementId }
        if (index != -1) {
            val target = currentList[index]
            currentList[index] = target.cloneWithNewText(op.newText, op.newFontSizePt)
            _activeElements.value = currentList
        }

        _undoStack.value = listOf(op) + _undoStack.value
        _healthReport.value = layoutGuardian.generateHealthReport(currentList)
    }

    suspend fun saveWorkingCopy() = withContext(Dispatchers.IO) {
        val doc = _activeDocument.value ?: return@withContext
        val elements = _activeElements.value

        when (doc.format) {
            DocumentFormat.DOCX -> {
                docxEngine.saveDocxSurgical(doc.workingFile, doc.workingFile, elements)
            }
            DocumentFormat.PDF -> {
                pdfEngine.savePdfHybrid(doc.workingFile, doc.workingFile, elements)
            }
            DocumentFormat.TXT -> {
                doc.workingFile.writeText(elements.joinToString("\n") { it.currentText })
            }
            DocumentFormat.UNKNOWN -> {}
        }

        _activeDocument.value = doc.copy(hasUnsavedChanges = false)
    }

    suspend fun exportToDestination(destFile: File): Boolean = withContext(Dispatchers.IO) {
        val doc = _activeDocument.value ?: return@withContext false
        val elements = _activeElements.value

        when (doc.format) {
            DocumentFormat.DOCX -> {
                docxEngine.saveDocxSurgical(doc.originalFile, destFile, elements)
            }
            DocumentFormat.PDF -> {
                pdfEngine.savePdfHybrid(doc.originalFile, destFile, elements)
            }
            DocumentFormat.TXT -> {
                destFile.writeText(elements.joinToString("\n") { it.currentText })
            }
            DocumentFormat.UNKNOWN -> return@withContext false
        }
        true
    }

    fun renderCurrentPageBitmap(pageIndex: Int, targetWidth: Int = 1080): Bitmap {
        val doc = _activeDocument.value ?: return Bitmap.createBitmap(targetWidth, (targetWidth * 1.33f).toInt(), Bitmap.Config.ARGB_8888)
        return when (doc.format) {
            DocumentFormat.PDF -> {
                pdfEngine.renderPageBitmap(doc.workingFile, pageIndex, _activeElements.value, targetWidth)
            }
            else -> {
                renderVectorPageBitmap(pageIndex, _activeElements.value, targetWidth)
            }
        }
    }

    fun renderOriginalPageBitmap(pageIndex: Int, targetWidth: Int = 1080): Bitmap {
        val doc = _activeDocument.value ?: return Bitmap.createBitmap(targetWidth, (targetWidth * 1.33f).toInt(), Bitmap.Config.ARGB_8888)
        val originalElements = _activeElements.value.map { it.copy(currentText = it.originalText, customFontSizePt = it.fontSizePt) }
        return when (doc.format) {
            DocumentFormat.PDF -> {
                pdfEngine.renderPageBitmap(doc.originalFile, pageIndex, emptyList(), targetWidth)
            }
            else -> {
                renderVectorPageBitmap(pageIndex, originalElements, targetWidth)
            }
        }
    }

    private fun renderVectorPageBitmap(pageIndex: Int, elements: List<TextElement>, targetWidth: Int): Bitmap {
        val targetHeight = (targetWidth * 1.294f).toInt()
        val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pageElements = elements.filter { it.pageIndex == pageIndex }

        for (elem in pageElements) {
            val left = elem.bounds.left * targetWidth
            val top = elem.bounds.top * targetHeight
            val right = elem.bounds.right * targetWidth
            val bottom = elem.bounds.bottom * targetHeight

            if (elem.containerType == com.example.layoutkeeper.model.ElementContainerType.TABLE_CELL) {
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E5E7EB")
                paint.strokeWidth = 2f
                canvas.drawRect(left - 6f, top - 4f, right + 6f, bottom + 4f, paint)
            }

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor(elem.colorHex)
            paint.textSize = elem.customFontSizePt * (targetWidth / 612f) * 1.33f
            paint.typeface = when {
                elem.isBold && elem.isItalic -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                elem.isBold -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                elem.isItalic -> Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                else -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val baseline = bottom - ((bottom - top) * 0.25f)
            canvas.drawText(elem.currentText, left, baseline, paint)
        }

        return bmp
    }

    // Generator helpers for realistic samples
    private fun createSampleDocxFile(file: File) {
        val documentXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:after="240"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="36"/><w:color w:val="1E3A8A"/></w:rPr><w:t>MASTER SERVICES &amp; CONSULTING AGREEMENT</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:spacing w:after="160"/></w:pPr>
      <w:r><w:rPr><w:sz w:val="22"/><w:color w:val="374151"/></w:rPr><w:t>This Agreement is entered into as of November 10, 2026, by and between Apex Systems LLC ("Client") and Vanguard Advisory Partners ("Consultant").</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:spacing w:after="120"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="24"/><w:color w:val="111827"/></w:rPr><w:t>SECTION 1. SCOPE OF SERVICES</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:spacing w:after="160"/></w:pPr>
      <w:r><w:rPr><w:sz w:val="22"/><w:color w:val="4B5563"/></w:rPr><w:t>The application provides a comprehensive document management system for all enterprise departments with zero data loss.</w:t></w:r>
    </w:p>
    <w:tbl>
      <w:tblPr><w:tblW w:w="5000" w:type="pct"/></w:tblPr>
      <w:tr>
        <w:tc><w:p><w:r><w:rPr><w:b/><w:sz w:val="20"/></w:rPr><w:t>Milestone Description</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:b/><w:sz w:val="20"/></w:rPr><w:t>Target Completion Date</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:b/><w:sz w:val="20"/></w:rPr><w:t>Professional Fee</w:t></w:r></w:p></w:tc>
      </w:tr>
      <w:tr>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>Phase 1 Core Architecture Audit</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>December 15, 2026</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>${'$'}15,000 USD</w:t></w:r></w:p></w:tc>
      </w:tr>
      <w:tr>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>Phase 2 Zero-Trust Security Rollout</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>January 30, 2027</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:rPr><w:sz w:val="20"/></w:rPr><w:t>${'$'}22,500 USD</w:t></w:r></w:p></w:tc>
      </w:tr>
    </w:tbl>
    <w:p>
      <w:pPr><w:spacing w:before="240" w:after="160"/></w:pPr>
      <w:r><w:rPr><w:i/><w:sz w:val="20"/><w:color w:val="6B7280"/></w:rPr><w:t>IN WITNESS WHEREOF, the parties hereto have executed this Agreement as of the effective date.</w:t></w:r>
    </w:p>
    <w:sectPr><w:pgSz w:w="12240" w:h="15840"/></w:sectPr>
  </w:body>
</w:document>""".trimIndent()

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>""".trimIndent()

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""".trimIndent()

        val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri"/><w:sz w:val="22"/></w:rPr></w:rPrDefault>
  </w:docDefaults>
</w:styles>""".trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            fun writeEntry(name: String, content: String) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
            writeEntry("[Content_Types].xml", contentTypesXml)
            writeEntry("_rels/.rels", relsXml)
            writeEntry("word/document.xml", documentXml)
            writeEntry("word/styles.xml", stylesXml)
        }
    }

    private fun createSamplePdfFile(file: File, isScanned: Boolean) {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (isScanned) {
            // Simulated scanned receipt layout
            paint.color = Color.parseColor("#F3F4F6")
            canvas.drawRect(50f, 60f, 562f, 730f, paint)

            paint.color = Color.parseColor("#1F2937")
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText("ACME LOGISTICS EXPRESS DELIVERY RECEIPT", 70f, 100f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("Tracking Number: 8492-9102-US", 70f, 150f, paint)
            canvas.drawText("Recipient: Metro Global Enterprise, Suite 400", 70f, 200f, paint)
            canvas.drawText("Package Weight: 4.80 kg | Freight Code: EXP-PRIORITY", 70f, 250f, paint)

            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 13f
            canvas.drawText("Total Customs & Freight Cleared: $1,420.00 USD", 70f, 310f, paint)

            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
            paint.textSize = 10f
            paint.color = Color.parseColor("#4B5563")
            canvas.drawText("Signature on file: Signed by Security Gate Officer", 70f, 580f, paint)

            // Simulated receipt lines
            paint.color = Color.parseColor("#D1D5DB")
            paint.strokeWidth = 2f
            canvas.drawLine(70f, 120f, 540f, 120f, paint)
            canvas.drawLine(70f, 340f, 540f, 340f, paint)
        } else {
            // Corporate invoice layout
            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("STRATEX TECHNOLOGIES INC. — QUARTERLY INVOICE", 60f, 90f, paint)

            paint.color = Color.parseColor("#4B5563")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Invoice No: INV-2026-0894 | Issue Date: Nov 10, 2026", 60f, 125f, paint)
            canvas.drawText("Bill To: Apex Global Solutions Corp, San Francisco, CA", 60f, 165f, paint)

            // Table header
            paint.color = Color.parseColor("#1E3A8A")
            paint.strokeWidth = 2f
            canvas.drawLine(60f, 190f, 550f, 190f, paint)

            paint.color = Color.parseColor("#111827")
            paint.textSize = 12f
            canvas.drawText("Item 1: Enterprise System Architecture & Code Audit: $12,500.00", 60f, 230f, paint)
            canvas.drawText("Item 2: High-Performance Database Tuning & Security Hardening: $8,400.00", 60f, 280f, paint)

            canvas.drawLine(60f, 310f, 550f, 310f, paint)

            paint.color = Color.parseColor("#047857")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("Total Due Net 30: $20,900.00 USD", 60f, 360f, paint)

            paint.color = Color.parseColor("#6B7280")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            canvas.drawText("Payment Terms: Wire transfer payable to Stratex Tech Operating Account.", 60f, 420f, paint)
        }

        pdfDoc.finishPage(page)
        FileOutputStream(file).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()
    }
}
