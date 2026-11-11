package com.example.layoutkeeper.engine.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.layoutkeeper.model.TextElement

class OcrEngine {

    data class OcrLineResult(
        val text: String,
        val confidence: Float,
        val boundingBox: RectF
    )

    fun recognizePageText(pageBitmap: Bitmap): List<OcrLineResult> {
        // Simulated on-device optical character analysis
        // In real production, this links to Android ML Kit TextRecognizer or Tesseract OCR
        val results = mutableListOf<OcrLineResult>()
        results.add(
            OcrLineResult(
                text = "ACME LOGISTICS EXPRESS DELIVERY RECEIPT",
                confidence = 0.98f,
                boundingBox = RectF(0.12f, 0.10f, 0.88f, 0.15f)
            )
        )
        results.add(
            OcrLineResult(
                text = "Tracking Number: 8492-9102-US",
                confidence = 0.96f,
                boundingBox = RectF(0.12f, 0.18f, 0.65f, 0.22f)
            )
        )
        results.add(
            OcrLineResult(
                text = "Recipient: Metro Global Enterprise, Suite 400",
                confidence = 0.95f,
                boundingBox = RectF(0.12f, 0.25f, 0.75f, 0.29f)
            )
        )
        results.add(
            OcrLineResult(
                text = "Package Weight: 4.80 kg | Freight Code: EXP-PRIORITY",
                confidence = 0.94f,
                boundingBox = RectF(0.12f, 0.32f, 0.82f, 0.36f)
            )
        )
        results.add(
            OcrLineResult(
                text = "Total Customs & Freight Cleared: $1,420.00 USD",
                confidence = 0.97f,
                boundingBox = RectF(0.12f, 0.40f, 0.78f, 0.44f)
            )
        )
        results.add(
            OcrLineResult(
                text = "Signature on file: Signed by Security Gate Officer",
                confidence = 0.91f,
                boundingBox = RectF(0.12f, 0.75f, 0.80f, 0.79f)
            )
        )
        return results
    }
}
