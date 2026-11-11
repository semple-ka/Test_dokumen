# LayoutKeeper Document Studio

LayoutKeeper Document Studio is a free, local-first document editing application that allows users to open existing PDF, DOCX, and text documents, make precise content modifications, and export them while strictly preserving original formatting, typography, and page structure.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core architectural decisions were confirmed during Phase 1:

- **Confirmed Decision 1 (Deployment Runtime)**: Native Android app powered by Jetpack Compose and embedded, zero-cost, local-only document processing engines with no external cloud or subscription dependencies.
- **Confirmed Decision 2 (PDF Editing Strategy)**: Hybrid PDF editing engine combining text block bounding-box extraction, visual canvas inspection, and exact-position text replacement with automatic overflow and boundary-collision detection.
- **Confirmed Decision 3 (DOCX Structure Preservation)**: Direct OOXML (Open Packaging Convention) document manipulation that edits targeted paragraph runs and table cells while preserving untouched XML structures, styles, relationships, media, headers, footers, and section properties.
- **Key Capability Principle**: The system strictly categorizes operations (Type 1 Native Edit, Type 2 Content Reconstruction, Type 3 Redaction & Replacement Overlay, Type 4 OCR Review) and never claims destructive overlays are native edits. Original files are protected and never overwritten automatically.

---

## 1. Overview & Core Concept

- **What It Does**: LayoutKeeper Document Studio gives users the power to make necessary text and data corrections in existing documents (contracts, invoices, reports, articles, and forms) without causing document layouts to reflow or breaking page geometries.
- **Target Audience**: Professionals, students, legal/finance operators, and mobile users who need reliable, offline-capable document corrections on mobile and tablet without sending confidential documents to third-party web converters or paying recurring subscription fees.
- **Key Value**: "Edit the existing document, do not rebuild its layout." It guarantees original file immutability, provides side-by-side visual difference verification, and detects potential text overflow before exporting.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Document Library & Ingestion**:
   - The user opens the app to their local document shelf showing recent documents, file sizes, format badges (PDF, DOCX, TXT), and preservation health indicators.
   - The user taps **Open Document** (using the Android Storage Access Framework) to import any document into an isolated, temporary working sandbox. The original source file remains untouched.
2. **Pre-Flight Layout Inspection & Analysis**:
   - The engine parses document metadata (page dimensions, orientation, margins, font tables, text blocks, embedded media, and tables).
   - If a PDF lacks selectable text, the app identifies it as a scanned document (Type 4) and offers on-device OCR review with side-by-side text verification.
3. **Interactive Document Studio & Page Workspace**:
   - A smooth, zoomable, multi-page document viewport with page boundary delineation and touch-friendly controls.
   - Tapping an existing paragraph, sentence, or table cell reveals the **Intelligent Text Editor** with live character-width metrics and fit indicators.
4. **Layout Protection & Overflow Manager**:
   - When text is modified, the editor immediately evaluates whether the replacement fits within the original bounding box:
     - **Option A (Original Size)**: Keeps font size, warns if text length exceeds threshold.
     - **Option B (Auto-Fit Within Tolerance)**: Adjusts font size within an approved 10–15% limit.
     - **Option C (Area Expansion)**: Expands bounded area only if no collision with adjacent elements is detected.
     - **Option D (Manual Verification)**: Visual inspection before committing.
5. **Original vs. Edited Comparison & Export**:
   - A dedicated **Diff & Verification Mode** provides synchronized side-by-side page previews, text change lists, and layout stability confirmation.
   - The user chooses **Export As** to save a new file copy to their device.

### Visual Identity & Theme

- **Aesthetic Direction**: *Precision Utilitarian*. Crisp, distraction-free document canvas framed by a modern Material 3 chrome with slate tones, deep sapphire accents, and emerald safety status indicators.
- **Color Palette**:
  - Primary Accent: Deep Slate Blue (`#1E3A8A` / `#3B82F6`)
  - Layout Guardian / Match: Emerald Green (`#059669` / `#10B981`)
  - Layout Warning / Overflow: Amber Gold (`#D97706` / `#F59E0B`)
  - Danger / Collision: Crimson Red (`#DC2626` / `#EF4444`)
  - Workspace Canvas: Soft Neutral Gray (`#F8FAFC` light / `#0F172A` dark)
  - Document Sheet: Pure White (`#FFFFFF`) with subtle ambient shadow elevation
- **Typography & Hierarchy**: Clean, high-legibility geometric headers paired with a monospace metric counter for bounding boxes, character counts, and font point sizes.
- **Interactive Feedback & Motion**: Smooth spring-based page zooming, animated warning pills for overflow alerts, and affirmative bottom sheet dialogues for destructive actions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct OOXML Processing vs. HTML-Based Document Conversion**
  - *Chosen Approach*: Direct OOXML package manipulation (using an embedded OpenXML parser and Apache POI-compatible streaming XML logic).
  - *Why*: Converting DOCX to HTML and back to DOCX irreversibly strips complex Word features (drawingml shapes, exact table column widths, pagination rules, headers/footers, and custom styles). Direct XML element mutation preserves 100% of the surrounding document markup.
  - *Alternatives Considered*: LibreOffice/Collabora server was rejected for standalone mobile use because running a heavy headless office suite server on-device consumes excessive memory and battery.
- **Decision 2: PDF Content Stream Analysis & Precise Replacement**
  - *Chosen Approach*: Structured page text extraction paired with vector text bounding-box replacement and canvas-layer reconciliation.
  - *Why*: PDF is a final-form display format with no inherent paragraph flow. Native stream edits without bounding-box checks cause text overlaps or clipping. Our hybrid engine calculates glyph bounds, renders the exact baseline, and warns before rendering.
  - *Alternatives Considered*: Pure text extraction into Markdown/PDF-rebuild was rejected because it completely destroys the original layout.
- **Decision 3: Zero-Overwriting File Protection Sandbox**
  - *Chosen Approach*: Immediate isolated copy to app cache on import. Edits are staged in an in-memory mutation log and local checkpoint database.
  - *Why*: Eliminates the risk of source file corruption if an unexpected crash or abort occurs.
- **Decision 4: Local-First Privacy Guarantee**
  - *Chosen Approach*: 100% on-device JVM and Android NDK processing. Zero external network calls or cloud API dependencies.
  - *Why*: Guarantees user privacy, works offline, zero API costs, and complies with enterprise document security guidelines.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                   LayoutKeeper Document Studio (UI)                    │
├─────────────────────┬──────────────────────────┬───────────────────────┤
│  Document Library   │  Interactive Workspace   │  Comparison & Diff    │
│  - Storage Picker   │  - Page Viewport / Zoom  │  - Synchronized Split │
│  - Recent Shelf     │  - Element Inspector     │  - Overflow Heatmap   │
│  - Document Health  │  - Smart Text Replacer   │  - Export Validator   │
└──────────┬──────────┴────────────┬─────────────┴───────────┬───────────┘
           │                       │                         │
           ▼                       ▼                         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Document Coordination Layer                       │
│ - DocumentSessionManager: Manages working copy lifecycle               │
│ - UndoRedoHistoryTracker: Multi-level mutation checkpoint tree         │
│ - LayoutIntegrityGuard: Real-time bounding-box & collision engine      │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
       ┌───────────────────────────┴───────────────────────────┐
       ▼                                                       ▼
┌───────────────────────────────┐       ┌────────────────────────────────┐
│      PDF Document Engine      │       │      DOCX Document Engine      │
│ - Text Extractor & Bounds Map │       │ - OOXML Package Streamer       │
│ - Content Classifier (Type1-4)│       │ - Paragraph & Run DOM Parser   │
│ - Precision Text Replacer     │       │ - Table Cell Content Editor    │
│ - On-Device OCR Reviewer      │       │ - Style & Media Preservation   │
│ - Android PdfRenderer Canvas  │       │ - Safe Package Repackager      │
└──────────────┬────────────────┘       └──────────────┬─────────────────┘
               │                                       │
               └───────────────────┬───────────────────┘
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   Storage & Persistence Foundation                     │
│ - Room Database: Document metadata, session states, and checkpoints    │
│ - App Sandbox Storage: Working copies (`/cache/docs/working_*.tmp`)    │
│ - Storage Access Framework (SAF): Safe user export to device storage   │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State Entities

- **DocumentItem**: `id`, `displayName`, `fileUri`, `mimeType`, `fileSizeBytes`, `lastModified`, `pageCount`, `formatHealthStatus`.
- **DocumentSession**: `sessionId`, `documentId`, `workingFilePath`, `originalFilePath`, `hasUnsavedChanges`, `activePageIndex`, `zoomScale`.
- **TextElementRef**: `elementId`, `pageIndex`, `originalText`, `currentText`, `bounds` (x, y, width, height), `fontFamily`, `fontSize`, `fontStyle`, `colorHex`, `alignment`, `containerType` (PARAGRAPH, TABLE_CELL, TEXT_BOX, PDF_LINE).
- **EditOperation**: `operationId`, `timestamp`, `elementRef`, `previousValue`, `newValue`, `operationClassification` (TYPE_1_NATIVE, TYPE_2_RECONSTRUCTION, TYPE_3_OVERLAY, TYPE_4_OCR), `layoutDelta` (widthChangeRatio, lineBreakCount).
- **LayoutHealthReport**: `hasOverflow`, `overflowDeltaPoints`, `collidingElementIds`, `missingFonts`, `unsupportedFeaturesList`, `validationState` (PASS, WARNING, BLOCKING).

### Interactive Component & State Mapping

- **DocumentLibraryScreen**: Handles SAF intent picker, displays local document cards with preservation ratings, triggers document ingestion into working sandbox.
- **WorkspaceScreen**:
  - Displays paginated document canvas via Compose `Canvas` and `LazyColumn` with pinch-to-zoom gestures (`rememberTransformableState`).
  - Overlay bounding boxes highlight editable elements on tap.
  - Floating bottom bar provides: Undo/Redo, Search & Replace, Page Navigator, Diff Mode, and Export.
- **TextReplacementSheet**: Modal bottom sheet triggered on element selection. Displays original text, live editable text field, character difference counter, bounding box width indicator bar, and the four overflow management options.
- **DiffViewerScreen**: Dual-pane or tabbed comparison rendering original page rasterization against current edited page state, highlighting changed coordinates in emerald/amber.
- **ExportDialog**: Validates document health, displays confirmation checklist, lets user choose file name and destination, and generates safe exported document.

---

## 5. Implementation Phases & Milestones

- **Phase 1: Foundation & Document Engines**:
  - Document ingestion via Android Storage Access Framework and safe sandboxing.
  - Core OOXML/DOCX package engine (read, inspect, surgically replace paragraph runs and table cells, repackage without altering styles/media).
  - Core PDF engine (extract page text blocks with bounding coordinates, render raster page previews, calculate text bounds).
  - Local session and checkpoint management with Room persistence.
- **Phase 2: Layout Guardian & Precise Editor UI**:
  - Interactive multi-page document viewport with zoom, pan, and page navigation.
  - Tap-to-select text element detection with visual bounding box highlights.
  - Smart text replacement sheet with live width/overflow calculation and 4-way fit strategies.
  - Undo/redo mutation stack.
- **Phase 3: Diff Comparison, OCR & Export Validation**:
  - Side-by-side visual difference viewer between original and edited states.
  - On-device text recognition preview for scanned/image-based documents.
  - Comprehensive pre-export layout validation report.
  - Safe export via SAF with original file immutability guarantee.
- **Phase 4: Acceptance Testing & Verification**:
  - Execution of non-negotiable test suite: Test A (Short replacement), Test B (Long replacement / overflow), Test C (Complex Word elements), Test D (Text-based PDF), Test E (Scanned document), Test F (Original file protection), Test G (Export and reopen).
