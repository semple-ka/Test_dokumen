# LayoutKeeper Document Studio

Free, Local-First Document Editor with Layout Preservation for Android.

LayoutKeeper Document Studio allows you to open existing PDF, Word DOCX, and text documents, make precise content modifications, and export them while strictly preserving original formatting, typography, and page structure.

---

## Key Features

- **Direct OOXML DOCX Manipulation**:
  - Surgical modifications to targeted text runs (`<w:t>`) and table cells (`<w:tc>`).
  - Preserves 100% of paragraph properties, margins, font tables, styles (`styles.xml`), tables, drawings, headers, footers, and embedded media.

- **Hybrid PDF Vector Engine**:
  - Classification into Type 1 (Native Content), Type 2 (Content Reconstruction), Type 3 (Redaction & Vector Overlay), and Type 4 (OCR Review).
  - High-DPI hardware page rendering via Android `PdfRenderer`.
  - In-place redaction and baseline-matched vector overlay without text bleed-through.

- **Intelligent Text Replacement & Layout Guardian**:
  - Real-time font metric bounds calculation.
  - Automatic overflow detection and width ratio monitoring.
  - Four controlled layout preservation strategies:
    - **Option A**: Preserve original font size with overflow warnings.
    - **Option B**: Auto-fit font size within 10–15% safe margin.
    - **Option C**: Expand bounded container if adjacent clearance permits.
    - **Option D**: Manual adjustment with live slider inspection.

- **Original File Protection**:
  - Working files are isolated in a local cache sandbox.
  - Source documents are never modified or overwritten automatically.
  - Multi-level Undo / Redo mutation tracking.

- **Visual Diff & Verification View**:
  - Side-by-side and changes-list comparison between Original and Current document states.
  - Real-time layout health audit and export readiness validation.

- **Acceptance Test Suite (Tests A–G)**:
  - Built-in verification dashboard running automated tests verifying layout preservation, table stability, overflow detection, and file immutability.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Repository pattern
- **Engines**:
  - `DocxEngine`: Open Packaging Conventions (OPC) ZIP & XML DOM parser
  - `PdfEngine`: Android `PdfRenderer`, vector `PdfDocument`, and text stream analyzer
  - `LayoutGuardian`: Typography bounds & collision detection engine
  - `OcrEngine`: Optical character review and inspection
- **Persistence**: Local Android App Sandbox Storage

---

## Building & Running

```bash
# Build Debug APK
gradle assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```

---

## Privacy & Security

LayoutKeeper is completely **local-first**:
- No external cloud document processing or conversion APIs.
- Zero tracking or analytics.
- Documents never leave your device without explicit user action.
