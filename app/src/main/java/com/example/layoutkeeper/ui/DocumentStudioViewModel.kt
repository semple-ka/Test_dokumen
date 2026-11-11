package com.example.layoutkeeper.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.layoutkeeper.data.DocumentRepository
import com.example.layoutkeeper.model.AcceptanceTestCase
import com.example.layoutkeeper.model.DocumentItem
import com.example.layoutkeeper.model.LayoutHealthReport
import com.example.layoutkeeper.model.OverflowStrategy
import com.example.layoutkeeper.model.TextElement
import com.example.layoutkeeper.testing.AcceptanceTestRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    LIBRARY,
    WORKSPACE,
    DIFF_VIEW,
    TEST_SUITE
}

class DocumentStudioViewModel(application: Application) : AndroidViewModel(application) {

    val repository = DocumentRepository(application)
    val testRunner = AcceptanceTestRunner(repository)

    private val _currentScreen = MutableStateFlow(AppScreen.LIBRARY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val documents = repository.documents
    val activeDocument = repository.activeDocument
    val activeElements = repository.activeElements
    val undoStack = repository.undoStack
    val redoStack = repository.redoStack
    val healthReport = repository.healthReport

    private val _selectedElement = MutableStateFlow<TextElement?>(null)
    val selectedElement: StateFlow<TextElement?> = _selectedElement.asStateFlow()

    private val _activePageIndex = MutableStateFlow(0)
    val activePageIndex: StateFlow<Int> = _activePageIndex.asStateFlow()

    private val _zoomScale = MutableStateFlow(1.0f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    private val _showSearchReplaceDialog = MutableStateFlow(false)
    val showSearchReplaceDialog: StateFlow<Boolean> = _showSearchReplaceDialog.asStateFlow()

    private val _showHealthDialog = MutableStateFlow(false)
    val showHealthDialog: StateFlow<Boolean> = _showHealthDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val testCases = testRunner.testCases
    val isRunningAllTests = testRunner.isRunningAll

    init {
        viewModelScope.launch {
            repository.initializeSamplesIfNeeded()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openDocument(item: DocumentItem) {
        viewModelScope.launch {
            repository.openDocument(item)
            _activePageIndex.value = 0
            _zoomScale.value = 1.0f
            _selectedElement.value = null
            _currentScreen.value = AppScreen.WORKSPACE
            showMessage("Opened ${item.title}")
        }
    }

    fun importDocument(uri: Uri, displayName: String) {
        viewModelScope.launch {
            try {
                val item = repository.importFromUri(uri, displayName)
                openDocument(item)
            } catch (e: Exception) {
                showMessage("Import failed: ${e.message}")
            }
        }
    }

    fun selectElement(element: TextElement?) {
        _selectedElement.value = element
    }

    fun applyElementEdit(
        elementId: String,
        newText: String,
        newFontSizePt: Float? = null,
        strategy: OverflowStrategy = OverflowStrategy.PRESERVE_SIZE_WARN
    ) {
        viewModelScope.launch {
            repository.applyEdit(elementId, newText, newFontSizePt, strategy)
            _selectedElement.value = null
            showMessage("Edit applied with ${strategy.title}")
        }
    }

    fun undo() {
        viewModelScope.launch {
            repository.undo()
            showMessage("Undo applied")
        }
    }

    fun redo() {
        viewModelScope.launch {
            repository.redo()
            showMessage("Redo applied")
        }
    }

    fun saveWorkingCopy() {
        viewModelScope.launch {
            try {
                repository.saveWorkingCopy()
                showMessage("Working copy saved safely")
            } catch (e: Exception) {
                showMessage("Save failed: ${e.message}")
            }
        }
    }

    fun exportDocument(customFileName: String) {
        viewModelScope.launch {
            val doc = activeDocument.value ?: return@launch
            val ext = doc.format.extension
            val targetName = if (customFileName.endsWith(".$ext")) customFileName else "$customFileName.$ext"
            val downloadsDir = File(getApplication<Application>().filesDir, "exports").apply { mkdirs() }
            val destFile = File(downloadsDir, targetName)

            val success = repository.exportToDestination(destFile)
            if (success) {
                _showExportDialog.value = false
                showMessage("Exported successfully to: ${destFile.name} (${destFile.length()} bytes)")
            } else {
                showMessage("Export failed")
            }
        }
    }

    fun performFindAndReplace(findText: String, replaceWith: String) {
        viewModelScope.launch {
            val elements = activeElements.value
            var replaceCount = 0
            for (elem in elements) {
                if (elem.currentText.contains(findText, ignoreCase = true)) {
                    val updated = elem.currentText.replace(findText, replaceWith, ignoreCase = true)
                    repository.applyEdit(elem.id, updated)
                    replaceCount++
                }
            }
            _showSearchReplaceDialog.value = false
            showMessage("Replaced in $replaceCount element(s)")
        }
    }

    fun setZoom(scale: Float) {
        _zoomScale.value = scale.coerceIn(0.5f, 3.0f)
    }

    fun setActivePage(page: Int) {
        val maxPage = (activeDocument.value?.pageCount ?: 1) - 1
        _activePageIndex.value = page.coerceIn(0, maxPage.coerceAtLeast(0))
    }

    fun toggleSearchReplaceDialog(show: Boolean) {
        _showSearchReplaceDialog.value = show
    }

    fun toggleHealthDialog(show: Boolean) {
        _showHealthDialog.value = show
    }

    fun toggleExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    fun runAllTests() {
        viewModelScope.launch {
            testRunner.runAllTests()
        }
    }

    fun runSingleTest(testId: String) {
        viewModelScope.launch {
            testRunner.runSingleTest(testId)
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }
}
