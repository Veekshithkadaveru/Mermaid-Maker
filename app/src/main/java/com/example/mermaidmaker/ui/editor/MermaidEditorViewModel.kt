package com.example.mermaidmaker.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.DiagramRepository
import com.example.mermaidmaker.domain.usecase.GenerateAiDiagramUseCase
import com.example.mermaidmaker.domain.usecase.GetBuiltInTemplatesUseCase
import com.example.mermaidmaker.domain.usecase.GetTemplatesByTypeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for MermaidEditor functionality
 */
class MermaidEditorViewModel(
    private val getBuiltInTemplatesUseCase: GetBuiltInTemplatesUseCase,
    private val getTemplatesByTypeUseCase: GetTemplatesByTypeUseCase,
    private val diagramRepository: DiagramRepository,
    private val generateAiDiagramUseCase: GenerateAiDiagramUseCase,
    private val fixMermaidCodeUseCase: com.example.mermaidmaker.domain.usecase.FixMermaidCodeUseCase,
    private val editorPreferences: com.example.mermaidmaker.data.local.prefs.EditorPreferences,
    private val fileExportService: com.example.mermaidmaker.domain.service.FileExportService
) : ViewModel() {

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _selectedDiagramType = MutableStateFlow(DiagramType.FLOWCHART)
    val selectedDiagramType: StateFlow<DiagramType> = _selectedDiagramType.asStateFlow()

    private val _availableTemplates = MutableStateFlow<List<Template>>(emptyList())
    val availableTemplates: StateFlow<List<Template>> = _availableTemplates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _fontSize = MutableStateFlow(14)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    private val _currentDiagramId = MutableStateFlow<String?>(null)
    val currentDiagramId: StateFlow<String?> = _currentDiagramId.asStateFlow()

    private val _diagramTitle = MutableStateFlow("")
    val diagramTitle: StateFlow<String> = _diagramTitle.asStateFlow()

    // AI generation state
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    private val _isAiAvailable = MutableStateFlow(false)
    val isAiAvailable: StateFlow<Boolean> = _isAiAvailable.asStateFlow()

    // AI fix state
    private val _isAiFixing = MutableStateFlow(false)
    val isAiFixing: StateFlow<Boolean> = _isAiFixing.asStateFlow()
    private val _aiFixErrorMessage = MutableStateFlow<String?>(null)
    val aiFixErrorMessage: StateFlow<String?> = _aiFixErrorMessage.asStateFlow()

    // Auto-save functionality
    private val _isAutoSaveEnabled = MutableStateFlow(true)
    val isAutoSaveEnabled: StateFlow<Boolean> = _isAutoSaveEnabled.asStateFlow()

    private val _lastAutoSaveTime = MutableStateFlow<Long?>(null)
    val lastAutoSaveTime: StateFlow<Long?> = _lastAutoSaveTime.asStateFlow()

    // PNG export state
    private val _isExportingPng = MutableStateFlow(false)
    val isExportingPng: StateFlow<Boolean> = _isExportingPng.asStateFlow()

    private val _isSharingPng = MutableStateFlow(false)
    val isSharingPng: StateFlow<Boolean> = _isSharingPng.asStateFlow()

    private val _pngExportResult = MutableStateFlow<Boolean?>(null)
    val pngExportResult: StateFlow<Boolean?> = _pngExportResult.asStateFlow()

    private var autoSaveJob: Job? = null
    private var lastContentSnapshot = ""
    private val AUTO_SAVE_INTERVAL_MS = 30_000L // 30 seconds

    init {
        loadTemplates()
        checkAiAvailability()
        startAutoSave()
        // Attempt to load the most recently edited diagram on startup
        viewModelScope.launch { loadMostRecent() }
    }

    /**
     * Load the most recently updated diagram (if any)
     */
    suspend fun loadMostRecent() {
        try {
            val lastId = editorPreferences.getLastOpenedDiagramId()
            val recent =
                if (lastId != null) diagramRepository.getDiagramById(lastId) else diagramRepository.getMostRecentDiagram()
            if (recent != null) {
                Log.d("MermaidEditorViewModel", "Loaded most recent diagram: ${recent.id}")
                _currentDiagramId.value = recent.id
                editorPreferences.setLastOpenedDiagramId(recent.id)
                _diagramTitle.value = recent.title
                _editorContent.value = recent.content
                _selectedDiagramType.value = recent.diagramType
                lastContentSnapshot = recent.content
            }
        } catch (t: Throwable) {
            Log.e("MermaidEditorViewModel", "Failed to load most recent diagram", t)
        }
    }

    /**
     * Load an existing diagram by ID
     */
    fun loadDiagram(diagramId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val diagram = diagramRepository.getDiagramById(diagramId)
                if (diagram != null) {
                    _currentDiagramId.value = diagramId
                    editorPreferences.setLastOpenedDiagramId(diagramId)
                    _diagramTitle.value = diagram.title
                    _editorContent.value = diagram.content
                    _selectedDiagramType.value = diagram.diagramType
                    // Initialize content snapshot for auto-save tracking
                    lastContentSnapshot = diagram.content
                    // Load templates for this diagram type
                    val templates = getTemplatesByTypeUseCase(diagram.diagramType).first()
                    _availableTemplates.value = templates.filter { it.isBuiltIn }
                } else {
                    _errorMessage.value = "Diagram not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load diagram: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update the editor content
     */
    fun updateContent(content: String) {
        _editorContent.value = content
    }

    /**
     * Clear the current error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Attempt to fix invalid Mermaid syntax using AI and update editor content on success
     */
    fun fixMermaidWithAi(source: String) {
        viewModelScope.launch {
            try {
                _isAiFixing.value = true
                _aiFixErrorMessage.value = null
                val result = fixMermaidCodeUseCase(source)
                if (result.isSuccess) {
                    val fixed = result.getOrThrow()
                    _editorContent.value = fixed
                } else {
                    _aiFixErrorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _aiFixErrorMessage.value = e.message ?: "Failed to fix with AI"
            } finally {
                _isAiFixing.value = false
            }
        }
    }

    /**
     * Save the current diagram (update if existing, create if new)
     */
    fun saveDiagram(onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val currentId = _currentDiagramId.value
                val title = _diagramTitle.value.ifEmpty { "Untitled Diagram" }
                val content = _editorContent.value

                if (currentId != null) {
                    // Update existing diagram
                    val existingDiagram = diagramRepository.getDiagramById(currentId)
                    if (existingDiagram != null) {
                        val updatedDiagram = existingDiagram.copy(
                            title = title,
                            content = content,
                            diagramType = _selectedDiagramType.value,
                            updatedAt = java.time.LocalDateTime.now()
                        )
                        diagramRepository.updateDiagram(updatedDiagram)
                        onSuccess("Diagram updated successfully!")
                    } else {
                        onError(Exception("Diagram not found"))
                    }
                } else {
                    onError(Exception("No diagram loaded to save. Use 'Create Diagram' to create a new one."))
                }
            } catch (e: Exception) {
                onError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set the diagram type and load corresponding templates
     */
    fun setDiagramType(diagramType: DiagramType) {
        _selectedDiagramType.value = diagramType
        loadTemplatesForType(diagramType)
    }

    /**
     * Load all built-in templates
     */
    private fun loadTemplates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val templates = getBuiltInTemplatesUseCase().first()
                _availableTemplates.value = templates
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load templates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load templates for a specific diagram type
     */
    private fun loadTemplatesForType(diagramType: DiagramType) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val templates = getTemplatesByTypeUseCase(diagramType).first()
                _availableTemplates.value = templates.filter { it.isBuiltIn }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load templates for $diagramType: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Get template content by template ID
     */
    fun getTemplateContent(templateId: String): String? {
        return _availableTemplates.value.find { it.id == templateId }?.content
    }


    /**
     * Clear the editor content
     */
    fun clearContent() {
        _editorContent.value = ""
    }

    /**
     * Copy content to clipboard
     */
    fun copyToClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mermaid Diagram", _editorContent.value)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Paste content from clipboard
     */
    fun pasteFromClipboard(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val pastedText = item.coerceToText(context)?.toString() ?: ""
            if (pastedText.isNotBlank()) {
                val current = _editorContent.value
                val newContent = if (current.isBlank()) pastedText else current + "\n" + pastedText
                _editorContent.value = newContent
                return newContent
            } else {
                _errorMessage.value = "Clipboard is empty or not text"
                return _editorContent.value
            }
        } else {
            _errorMessage.value = "Nothing to paste"
            return _editorContent.value
        }
    }

    /**
     * Set specific font size
     */
    fun setFontSize(size: Int) {
        _fontSize.value = size
    }

    /**
     * Generate a basic template for the current diagram type
     */
    fun generateBasicTemplate(): String {
        return when (_selectedDiagramType.value) {
            DiagramType.FLOWCHART -> """
                graph TD
                    A[Start] --> B[Process]
                    B --> C{Decision}
                    C -->|Yes| D[Action A]
                    C -->|No| E[Action B]
                    D --> F[End]
                    E --> F
            """.trimIndent()

            DiagramType.SEQUENCE -> """
                sequenceDiagram
                    participant User
                    participant System
                    participant Database
                    
                    User->>System: Request data
                    System->>Database: Query
                    Database-->>System: Result
                    System-->>User: Response
            """.trimIndent()

            DiagramType.CLASS -> """
                classDiagram
                    class Animal {
                        +String name
                        +int age
                        +makeSound()
                    }
                    
                    class Dog {
                        +bark()
                    }
                    
                    Animal <|-- Dog
            """.trimIndent()

            DiagramType.STATE -> """
                stateDiagram-v2
                    [*] --> Idle
                    Idle --> Processing : start
                    Processing --> Complete : finish
                    Processing --> Error : fail
                    Complete --> [*]
                    Error --> Idle : retry
            """.trimIndent()

            DiagramType.ER_DIAGRAM -> """
                erDiagram
                    USER {
                        int id PK
                        string name
                        string email
                    }
                    
                    POST {
                        int id PK
                        string title
                        text content
                        int user_id FK
                    }
                    
                    USER ||--o{ POST : creates
            """.trimIndent()

            DiagramType.JOURNEY -> """
                journey
                    title User Shopping Journey
                    section Discovery
                        Browse products: 5: User
                        Search for item: 4: User
                    section Purchase
                        Add to cart: 3: User
                        Checkout: 2: User, System
                        Payment: 1: User, Payment Gateway
            """.trimIndent()

            DiagramType.GANTT -> """
                gantt
                    title Project Timeline
                    dateFormat YYYY-MM-DD
                    
                    section Planning
                        Requirements: 2024-01-01, 5d
                        Design: after requirements, 7d
                    
                    section Development
                        Frontend: after design, 14d
                        Backend: after design, 10d
                        Testing: after frontend, 5d
            """.trimIndent()

            DiagramType.PIE -> """
                pie title Technology Usage
                    "JavaScript" : 40
                    "Python" : 25
                    "Java" : 20
                    "Other" : 15
            """.trimIndent()

            DiagramType.GITGRAPH -> """
                gitgraph
                    commit id: "Initial"
                    branch feature
                    commit id: "Feature work"
                    checkout main
                    commit id: "Hotfix"
                    checkout feature
                    commit id: "More feature work"
                    checkout main
                    merge feature
                    commit id: "Release"
            """.trimIndent()
        }
    }

    /**
     * Check if AI generation is available (has configured API keys)
     */
    private fun checkAiAvailability() {
        viewModelScope.launch {
            try {
                val isAvailable = generateAiDiagramUseCase.isAiGenerationAvailable()
                val providers = generateAiDiagramUseCase.getConfiguredProviders()
                Log.d("MermaidEditorViewModel", "AI Available: $isAvailable, Providers: $providers")
                _isAiAvailable.value = isAvailable
            } catch (e: Exception) {
                Log.e("MermaidEditorViewModel", "Error checking AI availability", e)
                _isAiAvailable.value = false
            }
        }
    }

    /**
     * Generate diagram from AI using natural language description
     */
    fun generateAiDiagram(prompt: String, diagramType: DiagramType) {
        viewModelScope.launch {
            try {
                _isAiGenerating.value = true
                _aiErrorMessage.value = null

                val result = generateAiDiagramUseCase(
                    prompt = prompt,
                    diagramType = diagramType.name,
                    provider = null // Use first available provider
                )

                if (result.isSuccess) {
                    val mermaidCode = result.getOrThrow()
                    Log.d("MermaidEditorViewModel", "Generated mermaid code: $mermaidCode")
                    _editorContent.value = mermaidCode
                    _selectedDiagramType.value = diagramType
                    Log.d("MermaidEditorViewModel", "Updated editor content successfully")
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to generate diagram"
                    Log.e("MermaidEditorViewModel", "AI generation failed: $errorMsg")
                    _aiErrorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                _aiErrorMessage.value = "Unexpected error: ${e.message}"
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    /**
     * Refresh AI availability status
     */
    fun refreshAiAvailability() {
        checkAiAvailability()
    }

    // Removed AI fix use case and method

    /**
     * Start auto-save timer
     */
    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_SAVE_INTERVAL_MS)
                if (_isAutoSaveEnabled.value) {
                    performAutoSave()
                }
            }
        }
    }

    /**
     * Perform auto-save if content has changed
     */
    private suspend fun performAutoSave() {
        try {
            val currentContent = _editorContent.value
            val currentId = _currentDiagramId.value

            // If no diagram exists yet but we have content, create one first
            if (currentId == null && currentContent.isNotBlank()) {
                val newDiagram = com.example.mermaidmaker.domain.model.MermaidDiagram(
                    id = java.util.UUID.randomUUID().toString(),
                    title = _diagramTitle.value.ifEmpty { "Untitled Diagram" },
                    content = currentContent,
                    diagramType = _selectedDiagramType.value,
                    createdAt = java.time.LocalDateTime.now(),
                    updatedAt = java.time.LocalDateTime.now(),
                    isFavorite = false
                )
                diagramRepository.insertDiagram(newDiagram)
                _currentDiagramId.value = newDiagram.id
                lastContentSnapshot = currentContent
                _lastAutoSaveTime.value = System.currentTimeMillis()
                Log.d(
                    "MermaidEditorViewModel",
                    "Auto-saved NEW diagram at ${java.time.LocalDateTime.now()}"
                )
                return
            }

            // Only auto-save updates if content has changed and we have a diagram loaded
            if (currentId != null && currentContent != lastContentSnapshot && currentContent.isNotBlank()) {
                val existingDiagram = diagramRepository.getDiagramById(currentId)
                if (existingDiagram != null) {
                    val updatedDiagram = existingDiagram.copy(
                        content = currentContent,
                        updatedAt = java.time.LocalDateTime.now()
                    )
                    diagramRepository.updateDiagram(updatedDiagram)
                    lastContentSnapshot = currentContent
                    _lastAutoSaveTime.value = System.currentTimeMillis()
                    Log.d(
                        "MermaidEditorViewModel",
                        "Auto-saved diagram at ${java.time.LocalDateTime.now()}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MermaidEditorViewModel", "Auto-save failed", e)
        }
    }

    /**
     * Enable or disable auto-save
     */
    fun setAutoSaveEnabled(enabled: Boolean) {
        _isAutoSaveEnabled.value = enabled
        if (enabled) {
            startAutoSave()
        } else {
            autoSaveJob?.cancel()
        }
    }

    /**
     * Trigger manual auto-save
     */
    fun triggerAutoSave() {
        if (_isAutoSaveEnabled.value) {
            viewModelScope.launch {
                performAutoSave()
            }
        }
    }

    /**
     * Export current diagram as PNG
     */
    fun exportDiagramAsPng(
        previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
        fileName: String = generateFileName("png")
    ) {
        viewModelScope.launch {
            try {
                _isExportingPng.value = true
                _pngExportResult.value = null
                
                val success = previewState.exportPNG(fileName, fileExportService)
                _pngExportResult.value = success
                
                Log.d(
                    "MermaidEditorViewModel", 
                    if (success) "PNG export successful" else "PNG export failed"
                )
            } catch (e: Exception) {
                Log.e("MermaidEditorViewModel", "Error during PNG export", e)
                _pngExportResult.value = false
            } finally {
                _isExportingPng.value = false
            }
        }
    }

    /**
     * Share current diagram as PNG
     */
    fun shareDiagramAsPng(
        previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
        fileName: String = generateFileName("png")
    ) {
        viewModelScope.launch {
            try {
                _isSharingPng.value = true
                
                val success = previewState.sharePNG(fileName, fileExportService)
                
                Log.d(
                    "MermaidEditorViewModel", 
                    if (success) "PNG share successful" else "PNG share failed"
                )
            } catch (e: Exception) {
                Log.e("MermaidEditorViewModel", "Error during PNG share", e)
            } finally {
                _isSharingPng.value = false
            }
        }
    }

    /**
     * Clear PNG export result state
     */
    fun clearPngExportResult() {
        _pngExportResult.value = null
    }

    /**
     * Generate filename for exports
     */
    private fun generateFileName(extension: String): String {
        val title = _diagramTitle.value.takeIf { it.isNotBlank() } ?: "diagram"
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "${sanitizedTitle}.${extension}"
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}