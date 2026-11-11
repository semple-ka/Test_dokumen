package com.example.layoutkeeper.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DocumentStudioApp(
    viewModel: DocumentStudioViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val activeDocument by viewModel.activeDocument.collectAsState()
    val activeElements by viewModel.activeElements.collectAsState()
    val selectedElement by viewModel.selectedElement.collectAsState()
    val undoStack by viewModel.undoStack.collectAsState()
    val redoStack by viewModel.redoStack.collectAsState()
    val activePageIndex by viewModel.activePageIndex.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    val healthReport by viewModel.healthReport.collectAsState()
    val testCases by viewModel.testCases.collectAsState()
    val isRunningAllTests by viewModel.isRunningAllTests.collectAsState()

    val showSearchReplaceDialog by viewModel.showSearchReplaceDialog.collectAsState()
    val showHealthDialog by viewModel.showHealthDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("app_navigation_bar")) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LIBRARY,
                    onClick = { viewModel.navigateTo(AppScreen.LIBRARY) },
                    icon = {
                        Icon(
                            if (currentScreen == AppScreen.LIBRARY) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Library"
                        )
                    },
                    label = { Text("Library") },
                    modifier = Modifier.testTag("nav_library_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.WORKSPACE,
                    onClick = {
                        if (activeDocument != null) {
                            viewModel.navigateTo(AppScreen.WORKSPACE)
                        } else if (documents.isNotEmpty()) {
                            viewModel.openDocument(documents.first())
                        }
                    },
                    icon = {
                        Icon(
                            if (currentScreen == AppScreen.WORKSPACE) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Studio Workspace"
                        )
                    },
                    label = { Text("Studio") },
                    modifier = Modifier.testTag("nav_workspace_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DIFF_VIEW,
                    onClick = { viewModel.navigateTo(AppScreen.DIFF_VIEW) },
                    icon = {
                        Icon(
                            if (currentScreen == AppScreen.DIFF_VIEW) Icons.Filled.Compare else Icons.Outlined.Compare,
                            contentDescription = "Diff Preview"
                        )
                    },
                    label = { Text("Diff") },
                    modifier = Modifier.testTag("nav_diff_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TEST_SUITE,
                    onClick = { viewModel.navigateTo(AppScreen.TEST_SUITE) },
                    icon = {
                        Icon(
                            if (currentScreen == AppScreen.TEST_SUITE) Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                            contentDescription = "Test Suite"
                        )
                    },
                    label = { Text("Tests") },
                    modifier = Modifier.testTag("nav_tests_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        documents = documents,
                        onOpenDocument = { viewModel.openDocument(it) },
                        onImportUri = { uri, name -> viewModel.importDocument(uri, name) }
                    )
                }
                AppScreen.WORKSPACE -> {
                    EditorWorkspaceScreen(
                        document = activeDocument,
                        elements = activeElements,
                        undoStack = undoStack,
                        redoStack = redoStack,
                        activePageIndex = activePageIndex,
                        zoomScale = zoomScale,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        onSelectElement = { viewModel.selectElement(it) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onSave = { viewModel.saveWorkingCopy() },
                        onOpenExport = { viewModel.toggleExportDialog(true) },
                        onOpenSearchReplace = { viewModel.toggleSearchReplaceDialog(true) },
                        onOpenHealthAudit = { viewModel.toggleHealthDialog(true) },
                        onOpenDiffView = { viewModel.navigateTo(AppScreen.DIFF_VIEW) },
                        onZoomChange = { viewModel.setZoom(it) },
                        onPageChange = { viewModel.setActivePage(it) }
                    )
                }
                AppScreen.DIFF_VIEW -> {
                    DiffComparisonScreen(
                        document = activeDocument,
                        elements = activeElements,
                        onBack = { viewModel.navigateTo(AppScreen.WORKSPACE) },
                        onOpenExport = { viewModel.toggleExportDialog(true) }
                    )
                }
                AppScreen.TEST_SUITE -> {
                    AcceptanceTestScreen(
                        testCases = testCases,
                        isRunningAll = isRunningAllTests,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        onRunAllTests = { viewModel.runAllTests() },
                        onRunSingleTest = { viewModel.runSingleTest(it) }
                    )
                }
            }

            // Interactive Text Replacement Bottom Sheet
            if (selectedElement != null) {
                TextReplacementSheet(
                    element = selectedElement!!,
                    pageElements = activeElements,
                    onDismiss = { viewModel.selectElement(null) },
                    onApplyEdit = { id, text, size, strategy ->
                        viewModel.applyElementEdit(id, text, size, strategy)
                    }
                )
            }

            // Search & Replace Dialog
            if (showSearchReplaceDialog) {
                SearchReplaceDialog(
                    onDismiss = { viewModel.toggleSearchReplaceDialog(false) },
                    onReplaceAll = { find, replace ->
                        viewModel.performFindAndReplace(find, replace)
                    }
                )
            }

            // Health Audit Dialog
            if (showHealthDialog) {
                LayoutHealthDialog(
                    report = healthReport,
                    onDismiss = { viewModel.toggleHealthDialog(false) }
                )
            }

            // Export Dialog
            if (showExportDialog) {
                ExportDialog(
                    document = activeDocument,
                    onDismiss = { viewModel.toggleExportDialog(false) },
                    onExport = { fileName ->
                        viewModel.exportDocument(fileName)
                    }
                )
            }
        }
    }
}
