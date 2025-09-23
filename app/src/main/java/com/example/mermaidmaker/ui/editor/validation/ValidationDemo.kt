package com.example.mermaidmaker.ui.editor.validation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.editor.ProfessionalSyntaxEditor

/**
 * Demo screen showcasing the professional Mermaid syntax validation features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationDemoScreen() {
    var selectedExample by remember { mutableStateOf(0) }
    
    val examples = listOf(
        "Valid Flowchart with Edge Labels" to """
            graph TD
                A[Start] --> B[Process]
                B --> C{Decision}
                C -->|Yes| D[Action A]
                C -->|No| E[Action B]
                D --> F[End]
                E --> F
        """.trimIndent(),
        
        "Missing Diagram Type" to """
            A[Start] --> B[Process]
            B --> C[End]
        """.trimIndent(),
        
        "Bracket Errors" to """
            graph TD
                A[Unclosed bracket --> B(Missing close
                C{Unbalanced --> D]Wrong bracket]
        """.trimIndent(),
        
        "Invalid Arrow Syntax" to """
            graph TD
                A[Start] -> B[Wrong arrow]
                C[Node] => D[Invalid arrow]
                E[Node] ->> F[Wrong for flowchart]
        """.trimIndent(),
        
        "Long Labels & Best Practices" to """
            graph TD
                A[This is a very long label that exceeds the recommended character limit for readability and should be shortened] --> B[Another Node]
                B --> C[Node]
                C --> D[Node]
                D --> E[Node]
                E --> F[Node]
                F --> G[Node]
                G --> H[Node]
                H --> I[Node]
                I --> J[Node]
                J --> K[Node]
                K --> L[Node]
                L --> M[Node]
                M --> N[Node]
                N --> O[Node]
                O --> P[Node]
                P --> Q[Node]
                Q --> R[Node]
                R --> S[Node]
                S --> T[Node]
                T --> U[Node]
                U --> V[Node]
        """.trimIndent(),
        
        "Valid Sequence Diagram" to """
            sequenceDiagram
                participant User
                participant System
                participant Database
                
                User->>System: Request data
                System->>Database: Query
                Database-->>System: Result
                System-->>User: Response
        """.trimIndent()
    )
    
    var content by remember { mutableStateOf(examples[selectedExample].second) }
    
    // Update content when example changes
    LaunchedEffect(selectedExample) {
        content = examples[selectedExample].second
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Professional Mermaid Validation Demo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Example selector
        Text(
            text = "Try different examples:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            examples.forEachIndexed { index, (title, _) ->
                FilterChip(
                    onClick = { selectedExample = index },
                    label = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    selected = selectedExample == index,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Features explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "✨ Professional Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val features = listOf(
                    "🔍 Advanced syntax validation with 15+ error types",
                    "⚠️ Three severity levels: Error, Warning, Info",
                    "📊 Smart error panel with filtering and categorization",
                    "💡 Contextual suggestions and quick fixes",
                    "🎯 Hover tooltips with detailed error descriptions",
                    "📝 Professional error highlighting in gutter",
                    "🔧 Best practice recommendations",
                    "📏 Code quality metrics and complexity analysis"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Professional Editor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            ProfessionalSyntaxEditor(
                content = content,
                fontSize = 14,
                onContentChanged = { content = it },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "💡 How to Use",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "• Click error indicators (●▲ⓘ) in the gutter for details\n" +
                            "• Long-press line numbers to see error tooltips\n" +
                            "• Click error count to open/close the detailed error panel\n" +
                            "• Use filter chips to narrow down error types\n" +
                            "• Try the 'Quick Fix' buttons for automatic corrections",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}