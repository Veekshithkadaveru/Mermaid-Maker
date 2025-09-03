package com.example.mermaidmaker.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.usecase.GetBuiltInTemplatesUseCase
import com.example.mermaidmaker.domain.usecase.GetTemplatesByTypeUseCase
import com.example.mermaidmaker.domain.repository.DiagramRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for MermaidEditor functionality
 */
class MermaidEditorViewModel(
    private val getBuiltInTemplatesUseCase: GetBuiltInTemplatesUseCase,
    private val getTemplatesByTypeUseCase: GetTemplatesByTypeUseCase,
    private val diagramRepository: DiagramRepository
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

    init {
        loadTemplates()
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
                    _diagramTitle.value = diagram.title
                    _editorContent.value = diagram.content
                    _selectedDiagramType.value = diagram.diagramType
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
     * Provide syntax suggestions based on current diagram type
     */
    fun getSyntaxSuggestions(): List<String> {
        return when (_selectedDiagramType.value) {
            DiagramType.FLOWCHART -> listOf(
                "graph TD",
                "graph LR", 
                "A[Rectangle]",
                "B(Round)",
                "C{Diamond}",
                "D((Circle))",
                "E>Flag]",
                "F[[Subroutine]]",
                "G[(Database)]",
                "A --> B",
                "B -.-> C",
                "C ==> D"
            )
            DiagramType.SEQUENCE -> listOf(
                "sequenceDiagram",
                "participant A as Actor",
                "participant B as Service",
                "A->>B: Request",
                "B-->>A: Response",
                "Note over A,B: Note text",
                "activate A",
                "deactivate A",
                "loop Loop condition",
                "alt Alternative",
                "opt Optional"
            )
            DiagramType.CLASS -> listOf(
                "classDiagram",
                "class Animal {",
                "  +String name",
                "  +makeSound()",
                "}",
                "Animal <|-- Dog",
                "Animal : +int age",
                "Animal : +makeSound()",
                "<<interface>> Animal"
            )
            DiagramType.STATE -> listOf(
                "stateDiagram-v2",
                "[*] --> State1",
                "State1 --> State2",
                "State2 --> [*]",
                "state State1 {",
                "  [*] --> SubState",
                "  SubState --> [*]",
                "}"
            )
            DiagramType.ER_DIAGRAM -> listOf(
                "erDiagram",
                "CUSTOMER {",
                "  string name",
                "  string address",
                "}",
                "ORDER {",
                "  int orderNumber",
                "  date orderDate",
                "}",
                "CUSTOMER ||--o{ ORDER : places"
            )
            DiagramType.JOURNEY -> listOf(
                "journey",
                "title User Journey",
                "section Shopping",
                "  Browse products: 5: User",
                "  Add to cart: 3: User",
                "  Checkout: 1: User, System"
            )
            DiagramType.GANTT -> listOf(
                "gantt",
                "title Project Timeline",
                "dateFormat YYYY-MM-DD",
                "section Development",
                "Design : 2023-01-01, 7d",
                "Code : after design, 14d"
            )
            DiagramType.PIE -> listOf(
                "pie title Survey Results",
                "\"Satisfied\" : 85",
                "\"Neutral\" : 10", 
                "\"Dissatisfied\" : 5"
            )
            DiagramType.GITGRAPH -> listOf(
                "gitgraph",
                "commit",
                "branch feature",
                "checkout feature",
                "commit",
                "checkout main",
                "merge feature"
            )
        }
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
    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            _editorContent.value = text
        }
    }

    /**
     * Increase font size
     */
    fun increaseFontSize() {
        if (_fontSize.value < 24) {
            _fontSize.value += 2
        }
    }

    /**
     * Decrease font size
     */
    fun decreaseFontSize() {
        if (_fontSize.value > 10) {
            _fontSize.value -= 2
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
}