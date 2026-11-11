package com.example.layoutkeeper.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.layoutkeeper.model.DocumentFormat
import com.example.layoutkeeper.model.DocumentItem
import com.example.layoutkeeper.model.EditOperation
import com.example.layoutkeeper.model.ElementContainerType
import com.example.layoutkeeper.model.TextElement
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorWorkspaceScreen(
    document: DocumentItem?,
    elements: List<TextElement>,
    undoStack: List<EditOperation>,
    redoStack: List<EditOperation>,
    activePageIndex: Int,
    zoomScale: Float,
    onBack: () -> Unit,
    onSelectElement: (TextElement) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenSearchReplace: () -> Unit,
    onOpenHealthAudit: () -> Unit,
    onOpenDiffView: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onPageChange: (Int) -> Unit
) {
    BackHandler { onBack() }

    var currentZoom by remember(zoomScale) { mutableFloatStateOf(zoomScale) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        val newZoom = (currentZoom * zoomChange).coerceIn(0.6f, 2.5f)
        currentZoom = newZoom
        onZoomChange(newZoom)
    }

    Scaffold(
        topBar = {
            TopAppBarWorkspace(
                document = document,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                onBack = onBack,
                onUndo = onUndo,
                onRedo = onRedo,
                onSearchReplace = onOpenSearchReplace,
                onHealthAudit = onOpenHealthAudit,
                onDiffView = onOpenDiffView,
                onSave = onSave,
                onExport = onOpenExport
            )
        },
        bottomBar = {
            BottomToolbar(
                activePageIndex = activePageIndex,
                totalPages = document?.pageCount ?: 1,
                currentZoom = currentZoom,
                hasUnsavedChanges = document?.hasUnsavedChanges == true,
                onPageChange = onPageChange,
                onZoomIn = {
                    val z = (currentZoom + 0.2f).coerceAtMost(2.5f)
                    currentZoom = z
                    onZoomChange(z)
                },
                onZoomOut = {
                    val z = (currentZoom - 0.2f).coerceAtLeast(0.6f)
                    currentZoom = z
                    onZoomChange(z)
                },
                onResetZoom = {
                    currentZoom = 1.0f
                    onZoomChange(1.0f)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .transformable(state = transformState)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .graphicsLayer(
                        scaleX = currentZoom,
                        scaleY = currentZoom
                    )
            ) {
                DocumentPaperSheet(
                    elements = elements,
                    pageIndex = activePageIndex,
                    onSelectElement = onSelectElement
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarWorkspace(
    document: DocumentItem?,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearchReplace: () -> Unit,
    onHealthAudit: () -> Unit,
    onDiffView: () -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document?.title ?: "Document Editor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (document?.hasUnsavedChanges == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmberWarning, CircleShape)
                        )
                    }
                }
                Text(
                    text = "${document?.format?.name ?: ""} • Layout Protected",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess,
                    fontSize = 10.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("nav_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library")
            }
        },
        actions = {
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.testTag("undo_button")) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.testTag("redo_button")) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
            }
            IconButton(onClick = onSearchReplace, modifier = Modifier.testTag("search_replace_button")) {
                Icon(Icons.Default.FindReplace, contentDescription = "Find and Replace")
            }
            IconButton(onClick = onHealthAudit, modifier = Modifier.testTag("health_audit_button")) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = "Layout Integrity Audit")
            }
            IconButton(onClick = onDiffView, modifier = Modifier.testTag("diff_view_button")) {
                Icon(Icons.Default.Compare, contentDescription = "Original vs Edited Diff")
            }
            IconButton(onClick = onSave, modifier = Modifier.testTag("save_button")) {
                Icon(Icons.Default.Save, contentDescription = "Save working copy")
            }
            IconButton(onClick = onExport, modifier = Modifier.testTag("export_button")) {
                Icon(Icons.Default.FileDownload, contentDescription = "Export final document", tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun DocumentPaperSheet(
    elements: List<TextElement>,
    pageIndex: Int,
    onSelectElement: (TextElement) -> Unit
) {
    val sheetWidth = 360.dp
    val sheetHeight = 520.dp

    Card(
        modifier = Modifier
            .size(sheetWidth, sheetHeight)
            .shadow(8.dp, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val pageElements = elements.filter { it.pageIndex == pageIndex }

            for (elem in pageElements) {
                val leftRatio = elem.bounds.left.coerceIn(0f, 1f)
                val topRatio = elem.bounds.top.coerceIn(0f, 1f)
                val widthRatio = (elem.bounds.right - elem.bounds.left).coerceIn(0.1f, 0.9f)
                val heightRatio = (elem.bounds.bottom - elem.bounds.top).coerceIn(0.03f, 0.2f)

                val itemColor = when {
                    elem.hasOverflow -> AmberWarning
                    elem.isDirty -> EmeraldSuccess
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widthRatio)
                            .height(sheetHeight * heightRatio)
                            .graphicsLayer {
                                translationX = (sheetWidth - 24.dp).toPx() * leftRatio
                                translationY = (sheetHeight - 24.dp).toPx() * topRatio
                            }
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (elem.isDirty) itemColor.copy(alpha = 0.08f) else Color.Transparent
                            )
                            .border(
                                width = if (elem.isDirty) 1.5.dp else 1.dp,
                                color = if (elem.isDirty) itemColor else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectElement(elem) }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = elem.currentText,
                            fontSize = (elem.customFontSizePt * 0.75f).sp,
                            fontWeight = if (elem.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = if (elem.fontFamily.contains("Courier", ignoreCase = true)) FontFamily.Monospace else FontFamily.SansSerif,
                            color = Color(android.graphics.Color.parseColor(elem.colorHex)),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (elem.isUnderline) TextDecoration.Underline else TextDecoration.None
                        )

                        // Visual indicator pill if modified
                        if (elem.isDirty) {
                            Surface(
                                color = itemColor,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(6.dp)
                                    .align(Alignment.TopEnd)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomToolbar(
    activePageIndex: Int,
    totalPages: Int,
    currentZoom: Float,
    hasUnsavedChanges: Boolean,
    onPageChange: (Int) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page navigation
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { onPageChange(activePageIndex - 1) },
                    enabled = activePageIndex > 0,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("◀", fontSize = 12.sp)
                }

                Text(
                    text = "Page ${activePageIndex + 1} of ${totalPages.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                OutlinedButton(
                    onClick = { onPageChange(activePageIndex + 1) },
                    enabled = activePageIndex < totalPages - 1,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("▶", fontSize = 12.sp)
                }
            }

            // Zoom controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "${(currentZoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onResetZoom)
                        .padding(horizontal = 4.dp)
                )
                IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
