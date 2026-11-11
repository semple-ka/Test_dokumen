MASTER PROMPT — LAYOUTKEEPER DOCUMENT STUDIO

Free, Local-First Document Editor with Layout Preservation

1. ROLE AND MISSION

Act as a Senior Full-Stack Engineer, Document Processing Engineer, Web Application Architect, and Document Format Specialist.

Your mission is to design and build a complete web-based document editing application called LayoutKeeper Document Studio.

The application must allow users to upload existing documents, edit their contents, and export the edited documents while preserving their original formatting and page layout as accurately as technically possible.

The central problem to solve:

Users should be able to change the text inside an existing PDF, Word document, or other supported document without destroying its original layout.

This application must prioritize free and open-source technologies. Avoid paid APIs, commercial document conversion services, mandatory subscriptions, and external cloud processing services.

The result must be a real working application, not a demonstration or static interface.

---

2. CORE PRODUCT PRINCIPLES

The following principles are mandatory:

1. Preserve the original document's layout.
2. Edit existing content instead of unnecessarily rebuilding the entire document.
3. Maintain formatting, positioning, spacing, and page structure wherever supported.
4. Preserve images, tables, headers, footers, and other document elements.
5. Never silently remove content or formatting.
6. Keep the original file unchanged until the user explicitly saves an edited copy.
7. Process documents locally or through a server controlled by the user.
8. Do not require a paid account or subscription.
9. Clearly identify any editing limitations before applying potentially destructive operations.

Important: Do not pretend that every file format can be edited with perfect fidelity. Preserve formatting to the maximum capability of the selected document engine and report any limitations honestly.

---

3. DOCUMENT ENGINE AND TECHNICAL ARCHITECTURE

Do not attempt to build an entire Word or PDF editing engine from scratch.

First, investigate and evaluate suitable free and open-source document editing technologies.

Possible candidates:

- ONLYOFFICE Docs Community Edition.
- Collabora Online.
- LibreOffice-based processing tools.
- Other suitable open-source document editing engines.

Evaluate each candidate based on:

- Actual editing capabilities.
- PDF editing support.
- DOCX compatibility.
- Format preservation.
- Browser integration.
- Self-hosting requirements.
- Licensing conditions.
- Mobile browser compatibility.
- Resource requirements.
- Whether commercial features are required for the intended functionality.

Prefer a proven document editor engine rather than attempting to reproduce a complete office suite with basic HTML and JavaScript.

For the primary architecture, consider:

Frontend

- HTML5.
- CSS3.
- JavaScript.
- Responsive document workspace.
- Integrated document editor interface.

Backend, if necessary

- Python with FastAPI or another appropriate lightweight backend.
- Secure file upload and temporary storage.
- Document engine integration.
- Save and export management.
- Document conversion only when necessary.

Document editing engine

- Prefer a free, open-source, self-hosted solution.
- Do not depend on paid cloud APIs.
- Verify all licensing obligations before implementation.
- Keep the document engine replaceable so another compatible engine can be integrated later.

The frontend should remain an HTML-based web application, but do not force the entire architecture into one HTML file if doing so would compromise real editing functionality.

---

4. DOCUMENT FORMAT SUPPORT

Support document formats according to the actual capabilities of the chosen engine.

Prioritize:

Format| Expected functionality
PDF| View, edit supported content, annotate, export
DOCX| Edit text and formatting
DOC| Open and convert where supported
ODT| Edit where supported
RTF| Edit where supported
TXT| Edit text
HTML| Edit where supported
PPTX| Support if the chosen engine provides it
XLSX| Support if the chosen engine provides it
PNG/JPG| Import as visual document or image-based PDF where supported

Provide a clear indication of which formats are natively editable, converted before editing, view-only, or unsupported.

Do not show an Edit button for formats that cannot actually be edited.

---

5. PDF EDITING ENGINE

PDF editing is one of the application's most important features.

Distinguish between two types of PDF:

A. Text-Based PDF

This type contains selectable text and structured text elements.

Required behavior:

- Allow editing of supported existing text.
- Maintain original text positions as much as possible.
- Preserve font appearance and text size where available.
- Preserve images and graphical elements.
- Maintain page dimensions and positioning.
- Avoid shifting unrelated content.
- Keep the original page structure.

B. Scanned or Image-Based PDF

This type contains page images rather than selectable text.

Required behavior:

- Detect whether a PDF page is scanned.
- Offer an OCR workflow when text recognition is necessary.
- Consider Tesseract OCR or another suitable open-source OCR engine.
- Display recognized text for review.
- Allow users to correct OCR mistakes.
- Preserve the original page image as much as possible.
- Clearly distinguish original image content from editable OCR text.

Do not automatically assume that scanned PDFs can be edited like normal text-based PDFs.

PDF Editing Safety

Do not simply extract every word, convert the PDF into an HTML text document, and recreate the PDF.

Do not use a text-overlay technique as a substitute for genuine text editing while claiming the original content was modified.

When an operation requires redaction, replacement, or reconstruction of page content, explicitly identify the operation and provide a preview before applying it.

---

6. WORD DOCUMENT EDITING ENGINE

DOCX and other supported word-processing formats should retain their existing document structure.

Preserve the following wherever possible:

- Page dimensions.
- Page margins.
- Paragraph formatting.
- Font families.
- Font sizes.
- Font colors.
- Bold, italic, and underline.
- Text alignment.
- Line spacing.
- Paragraph spacing.
- Numbered lists.
- Bulleted lists.
- Tables.
- Images.
- Image positioning.
- Text wrapping.
- Headers and footers.
- Page numbering.
- Section breaks.
- Page breaks.
- Hyperlinks.
- Footnotes and endnotes.
- Document styles.

When editing a sentence or paragraph, avoid changing unrelated sections.

Do not automatically replace the original document's styles with generic browser formatting.

Preserve the document's original OOXML structure whenever the selected editing engine supports it.

---

7. DOCUMENT WORKSPACE UI

Build a professional document-editing interface.

The main workspace should contain:

A. Document Library

- Upload document.
- Recently opened documents.
- File name.
- File type.
- File size.
- Last modified information.
- Remove temporary document.

B. Document Editor

- Display the original document in a page-based editing environment.
- Maintain page boundaries.
- Provide zoom controls.
- Support scrolling between pages.
- Preserve the editing engine's native formatting capabilities.

C. Editing Toolbar

- Undo and redo.
- Save.
- Save as.
- Download.
- Zoom in and out.
- Find text.
- Replace text.
- Page navigation.
- Fullscreen.

D. Layout Preview

- Show document pages before and after editing when practical.
- Allow page-by-page inspection.
- Highlight potential layout changes when detectable.

The document itself must remain the primary focus. Avoid unnecessary decorative elements that reduce workspace space.

---

8. SMART TEXT EDITING

Implement useful editing capabilities:

- Find and replace.
- Replace selected text.
- Edit paragraphs.
- Edit table contents.
- Select and modify formatting.
- Preserve nearby layout.
- Undo and redo.
- Track unsaved changes.

When replacing text, preserve the surrounding formatting as much as the document engine allows.

Example:

Original:

"The application provides a comprehensive document management system."

Edited:

"The application provides a professional document editing system."

The replacement should not unnecessarily change the paragraph's font, alignment, margins, or position.

---

9. ORIGINAL FILE PROTECTION

Implement a safe editing workflow.

- Never overwrite the original file automatically.
- Create a working copy when a document is opened.
- Support Save and Save As.
- Provide a clear unsaved-changes indicator.
- Warn before discarding unsaved edits.
- Keep temporary files under control.
- Allow users to download the final edited document.
- Provide recovery or previous versions when practical.

The application must not silently corrupt the source document.

---

10. LOCAL-FIRST PRIVACY AND COST

Privacy and avoiding paid services are major requirements.

- Do not upload documents to third-party conversion websites.
- Do not require AI APIs.
- Do not require cloud subscriptions.
- Prefer local processing or a self-hosted document server.
- Do not transmit document contents to external services without explicit user authorization.
- Do not include tracking or analytics that expose document contents.

Use free and open-source dependencies with verifiable licenses.

Clearly document which components run inside the browser and which require a local or self-hosted server.

Important: A free and open-source application may still require server hardware, storage, or maintenance. Do not present those operational requirements as zero-cost.

---

11. MOBILE AND DESKTOP COMPATIBILITY

The application must work on:

- Android browsers.
- Chrome.
- Edge.
- Firefox.
- Desktop computers.
- Tablets.

Mobile requirements:

- Responsive editing workspace.
- Touch-friendly controls.
- Scrollable pages.
- File upload from device storage.
- Zoom and page navigation.
- No reliance on hover-only interactions.
- Reasonable memory consumption.
- Avoid excessive background processing.

If the document engine requires a separate server, provide a deployment option where a PC or Linux machine hosts the editor and the user accesses it from an Android browser over the local network.

Do not assume that Docker or a full document server can run directly on every Android device.

---

12. ERROR HANDLING AND DOCUMENT INTEGRITY

Handle the following situations:

- Corrupted files.
- Unsupported file formats.
- Password-protected documents.
- Encrypted PDFs.
- Missing fonts.
- Unsupported document features.
- Large documents.
- OCR failures.
- Failed document conversions.
- Network disconnections.
- Save failures.
- Insufficient storage.
- Document engine unavailable.

Display understandable messages and offer safe recovery options.

Never report a successful save or conversion unless the operation actually completed.

---

13. PERFORMANCE REQUIREMENTS

Optimize the application for practical use.

- Avoid loading unnecessary document data.
- Use efficient temporary file management.
- Avoid blocking the UI during processing.
- Provide loading and progress indicators.
- Process large documents carefully.
- Release temporary resources.
- Avoid memory leaks.
- Handle multiple document pages efficiently.

Use appropriate server-side processing when browser memory or performance is insufficient.

---

14. TESTING AND VERIFICATION

Create a document compatibility test suite.

Test with representative files containing:

1. Simple paragraphs.
2. Multiple fonts and font sizes.
3. Tables.
4. Images.
5. Headers and footers.
6. Multiple sections.
7. Different page orientations.
8. Multi-page layouts.
9. Text-based PDFs.
10. Scanned PDFs.
11. DOCX files with complex formatting.

For each supported file type:

- Open the original file.
- Record or inspect its original layout.
- Edit a small section.
- Save the edited document.
- Reopen the exported file.
- Compare layout and text.
- Check whether unrelated content was changed.
- Report any formatting losses.

Do not claim pixel-perfect preservation without actual validation.

---

15. IMPLEMENTATION RULES FOR THE CODING AI

Work as an engineer building a real product.

Do not respond with only a theoretical architecture or a visual prototype.

You must:

1. Analyze the technical requirements.
2. Select an appropriate free document editing engine.
3. Explain the architecture and its limitations.
4. Define the complete project structure.
5. Implement the frontend.
6. Implement the backend if required.
7. Integrate the document engine.
8. Implement actual upload, editing, saving, and exporting.
9. Provide complete configuration files.
10. Provide installation and deployment instructions.
11. Test the implementation where tools are available.
12. Report verified features and unverified features separately.

Never generate fake document editing functionality.

Never use a placeholder editor and claim that it preserves PDF or Word formatting.

Never omit important source files.

When generating code, provide complete file contents, not isolated snippets or incomplete functions.

If output limits are reached, continue from the exact point where generation stopped without restarting or omitting code.

Do not silently substitute a paid API or commercial-only feature when a free implementation is required.

If a requested feature cannot be implemented with the chosen free engine, explain why and provide the closest genuine alternative.

---

16. FINAL PRODUCT VISION

The finished application should function as a practical document editing workspace.

The user uploads an existing PDF, Word document, or another supported format.

The document opens in a genuine editing environment.

The user changes its text while keeping the original layout, formatting, images, and page structure intact as far as the editing engine permits.

The user saves and downloads the edited document without having to subscribe to a paid online document editing service.

THE MAIN OBJECTIVE: EDIT THE EXISTING DOCUMENT, NOT REBUILD ITS ENTIRE LAYOUT.

Build a reliable, privacy-conscious, free, and extensible document editing application.