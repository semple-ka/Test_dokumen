ADDITIONAL ENGINEERING INSTRUCTIONS — LAYOUTKEEPER DOCUMENT STUDIO

Advanced Document Fidelity, Editing Precision, Security & Quality Assurance

These instructions extend the main LayoutKeeper Document Studio specification.

They are mandatory engineering requirements, not optional suggestions.

---

17. DOCUMENT FIDELITY PROTECTION ENGINE

Build a dedicated system that prioritizes the preservation of original document appearance.

Before editing a document, inspect and establish its original visual and structural characteristics.

Where technically possible, identify:

- Page dimensions.
- Page orientation.
- Margins.
- Text block positions.
- Text bounding boxes.
- Font families.
- Font sizes.
- Font weights.
- Line heights.
- Paragraph boundaries.
- Text alignment.
- Image locations.
- Table boundaries.
- Header and footer positions.
- Page breaks.
- Section boundaries.
- Background elements.

Create an internal document representation that allows the application to track the relationship between the original document elements and the edited elements.

Do not modify unrelated elements merely because the user changes one paragraph.

Layout Preservation Rules

When the user changes text:

1. Preserve the original text block position whenever possible.
2. Preserve font properties and paragraph formatting.
3. Preserve neighboring content.
4. Preserve images and tables.
5. Preserve page dimensions.
6. Preserve existing page breaks where possible.
7. Detect text overflow.
8. Detect unexpected page expansion.
9. Detect displaced or overlapping elements.
10. Warn the user before applying a potentially destructive layout adjustment.

Never silently sacrifice document integrity to make an edit appear successful.

---

18. INTELLIGENT TEXT REPLACEMENT ENGINE

Implement a precise text replacement mechanism.

When users replace existing text, the application must distinguish between:

- Replacing a short word.
- Replacing a sentence.
- Replacing a paragraph.
- Replacing text inside a table.
- Replacing text inside a positioned text box.
- Replacing text inside a PDF page.

The system should preserve the original formatting of the selected text.

Text Length Management

A replacement may be longer or shorter than the original text.

The application must detect whether the new text fits within the original area.

If it does not fit, provide controlled options:

Option A — Preserve Original Size

Keep the original font size and formatting. Warn if the text overflows.

Option B — Adjust Text Within Limits

Allow a small font-size adjustment within a reasonable configurable range.

Never shrink text excessively without user approval.

Option C — Expand Text Area

Where the format permits, expand the text area while checking for collisions with nearby content.

Option D — Manual Adjustment

Let the user inspect and correct the result in the document editor.

The application must never silently remove words, truncate content, or hide overflow text.

---

19. PDF EDITING CAPABILITY CLASSIFICATION

PDF editing must use an explicit capability classification system.

Every PDF editing operation must be identified as one of these types:

Type 1 — Native Content Editing

The engine genuinely modifies supported existing PDF text content.

Use this approach whenever the selected engine supports it.

Type 2 — Content Reconstruction

The original content is modified by reconstructing an affected text region or document element.

The application must preserve surrounding elements and provide a preview.

Type 3 — Redaction and Replacement Overlay

The original visible text is removed or covered through an explicit redaction operation, and replacement text is inserted in its place.

This must not be represented as native editing if the underlying text was not actually modified.

Type 4 — OCR-Based Editing

The document is an image-based or scanned PDF.

The application uses OCR to identify text, but must preserve the original page image and provide a review process.

Mandatory PDF Rules

- Identify the editing method used.
- Display limitations clearly.
- Never claim true text editing when only an overlay is being applied.
- Do not leave the original text visible underneath replacement content.
- Do not create overlapping text.
- Do not accidentally cover unrelated images or graphics.
- Provide an original-versus-edited preview.
- Allow users to cancel before destructive changes.

For digital signatures, encrypted documents, and certified PDFs, detect restrictions and warn users that editing may invalidate signatures or document certification.

---

20. DOCX STRUCTURE PRESERVATION

For Word documents, avoid unnecessary conversion through plain text or generic HTML.

When possible, preserve the original DOCX package structure and its relationships.

Pay particular attention to:

- Document XML.
- Styles XML.
- Numbering definitions.
- Relationships.
- Embedded images.
- Headers and footers.
- Section properties.
- Tables.
- Hyperlinks.
- Footnotes.
- Endnotes.
- Comments.
- Track changes.
- Fields.
- Page layout settings.

Do not remove unsupported OOXML elements merely because the application does not display or edit them.

If the selected document engine cannot preserve a specific feature, identify the limitation and warn the user before export.

When editing a document, preserve the original template and styles instead of applying a new generic template.

---

21. ORIGINAL VS EDITED DOCUMENT COMPARISON

Develop a document comparison system.

The user should be able to inspect the original and edited result.

Provide:

- Side-by-side page previews.
- Before-and-after text comparison where supported.
- Page count comparison.
- Text overflow warnings.
- Missing image detection where possible.
- Changed page-break detection.
- Shifted element detection where measurable.
- Unexpected formatting changes.
- Export validation status.

Use visual rendering comparisons when practical.

Do not rely solely on text comparison to determine whether a document's layout has been preserved.

The comparison system must distinguish between:

- Expected changes caused by user edits.
- Unexpected changes caused by conversion or document processing.

---

22. DOCUMENT CHANGE TRACKING AND RECOVERY

Implement a reliable editing history.

Required functionality:

- Undo.
- Redo.
- Save checkpoints.
- Unsaved-change detection.
- Restore previous working state.
- Cancel an operation.
- Revert selected changes where supported.
- Create a new version after successful export.

Maintain a distinction between:

1. Original document.
2. Working document.
3. Saved working version.
4. Exported final document.

The original file must remain untouched.

When a document engine supports native revision history or tracked changes, preserve those capabilities where practical.

---

23. DOCUMENT SECURITY AND PRIVACY HARDENING

Uploaded documents must be treated as untrusted input.

Implement appropriate protections against:

- Malformed documents.
- Malicious document content.
- Archive and ZIP bombs.
- Oversized files.
- Path traversal.
- Unsafe file names.
- Unauthorized file access.
- Cross-site scripting.
- Unsafe embedded HTML.
- Unauthorized access to temporary documents.
- Accidental exposure of documents over the local network.

Additional requirements:

- Validate file types and content instead of trusting extensions alone.
- Apply configurable file-size limits.
- Generate safe internal file names.
- Store temporary documents in isolated locations.
- Restrict access to files by user or session.
- Do not execute document macros.
- Avoid automatically loading external resources embedded in documents.
- Use appropriate upload and download protections.
- Clean up expired temporary files.
- Do not expose internal storage paths to the browser.

For server-based installations, document the authentication and network-security requirements.

A locally hosted application must not automatically expose private documents to every device on the network.

---

24. OPEN-SOURCE AND DEPENDENCY GOVERNANCE

Do not select technologies solely because they appear easy to integrate.

For every major dependency, evaluate:

- License.
- Commercial-use conditions.
- Redistribution requirements.
- Source availability.
- Maintenance activity.
- Security history where available.
- Actual document-format capabilities.
- Integration complexity.
- Resource consumption.

Pin and document dependency versions.

Verify the latest official documentation when integrating third-party engines.

Do not invent APIs, configuration parameters, or undocumented capabilities.

Maintain an architecture that allows the document engine to be replaced without rewriting the entire frontend.

If a dependency has a paid enterprise feature, do not assume that the feature is available in the free edition.

Clearly distinguish free functionality from commercial functionality.

---

25. SMART FEATURE PRIORITIZATION

Do not attempt to implement every office-document feature simultaneously.

Use a phased development strategy.

Phase 1 — Functional Foundation

- Upload PDF and DOCX.
- Display documents.
- Integrate a genuine editing engine.
- Basic document editing.
- Save and download.
- Preserve the original file.
- Basic error handling.

Phase 2 — Layout Preservation

- Precise text replacement.
- PDF editing classification.
- OCR support.
- Layout comparison.
- Overflow detection.
- Formatting validation.

Phase 3 — Professional Editing

- Advanced document comparison.
- Revision management.
- More document formats.
- Advanced tables and image handling.
- Improved mobile experience.

Phase 4 — Advanced Processing

- Batch document processing.
- Document conversion.
- Advanced OCR.
- Document recovery.
- Advanced version management.

Finish and validate each phase before claiming it is complete.

Do not create buttons for features that have not been implemented.

---

26. ACCEPTANCE TESTS — NON-NEGOTIABLE

The final product must pass practical document-editing tests.

Test A — Short Text Replacement

Open an existing DOCX document.

Replace one word.

Verify that:

- Other text remains unchanged.
- Font properties remain unchanged.
- Paragraph alignment remains unchanged.
- Page layout remains stable.

Test B — Long Text Replacement

Replace a short sentence with a longer sentence.

Verify that:

- No words disappear.
- Text overflow is detected.
- Layout problems are reported.
- Any adjustment requires appropriate user control.

Test C — Complex Word Document

Open a DOCX containing images, tables, headers, footers, multiple fonts, and page breaks.

Edit one paragraph.

Verify that unrelated document elements remain intact.

Test D — Text-Based PDF

Open a PDF containing selectable text.

Edit supported existing text.

Verify that:

- The method of editing is correctly identified.
- Unrelated page elements remain in place.
- The replacement does not overlap other content.
- The exported PDF opens successfully.

Test E — Scanned PDF

Open a scanned document.

Verify that:

- OCR is correctly identified as necessary.
- The user can inspect recognized text.
- Original page images are preserved.
- OCR errors can be corrected.
- The editing method is explained.

Test F — Original File Protection

Open a document and make changes.

Close or cancel without saving.

Verify that the original document remains unchanged.

Test G — Export and Reopen

Save the edited document.

Reopen it with the application or a compatible external document viewer.

Verify text, layout, images, tables, and page structure.

Acceptance Rule

A feature is complete only when it has been implemented and the corresponding test has been performed.

Clearly report:

- Passed tests.
- Failed tests.
- Partially supported features.
- Untested features.
- Known limitations.

Never fabricate test results.

---

27. FINAL ENGINEERING DIRECTIVE

Your primary responsibility is document integrity.

A visually attractive application that corrupts uploaded documents is a failed implementation.

A document that allows text editing but destroys its original formatting is also a failed implementation.

Prioritize the following order:

1. Correct document handling.
2. Accurate text editing.
3. Layout preservation.
4. Data integrity.
5. Export reliability.
6. Security and privacy.
7. User experience.
8. Additional features.

Always prefer a genuine, verified document editing capability over a visually impressive but fake implementation.

FINAL PRINCIPLE: MODIFY ONLY WHAT THE USER INTENDS TO MODIFY. PRESERVE EVERYTHING ELSE WHENEVER THE DOCUMENT FORMAT AND ENGINE ALLOW IT.