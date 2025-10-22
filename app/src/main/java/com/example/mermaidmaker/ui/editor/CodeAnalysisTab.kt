package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.data.ai.CodeLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeAnalysisTab(
    onGenerateFromCode: (String, CodeLanguage) -> Unit,
    isGenerating: Boolean,
    errorMessage: String?,
    isAiAvailable: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var codeInput by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.KOTLIN) }
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showPasteDialog by remember { mutableStateOf(false) }
    var manualPaste by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Code to Diagram Generator",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Paste your code and generate Mermaid diagrams automatically. Supports Kotlin, Java, SQL, JavaScript, Python, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // AI availability status
        if (!isAiAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Generation Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Please configure your AI provider in settings to use code analysis features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh AI status",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Language selection
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLanguage.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = { },
                readOnly = true,
                label = { Text("Programming Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                CodeLanguage.entries.filter { it != CodeLanguage.UNKNOWN }.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            selectedLanguage = language
                            expanded = false
                        }
                    )
                }
            }
        }

        // Code input field
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Paste your ${selectedLanguage.name.lowercase()} code here") },
            placeholder = { Text(getPlaceholderText(selectedLanguage)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp),
            singleLine = false,
            minLines = 10,
            maxLines = Int.MAX_VALUE,
            trailingIcon = {
                IconButton(onClick = {
                    val composeText = clipboardManager.getText()?.text
                    val fallback = com.example.mermaidmaker.ui.common.ClipboardUtils.pasteText(context)
                    val toPaste = when {
                        !composeText.isNullOrBlank() -> composeText
                        !fallback.isNullOrBlank() -> fallback
                        else -> null
                    }
                    if (toPaste != null) {
                        codeInput = if (codeInput.isBlank()) toPaste else codeInput + "\n" + toPaste
                    } else {
                        showPasteDialog = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from clipboard"
                    )
                }
            }
        )

        if (showPasteDialog) {
            AlertDialog(
                onDismissRequest = { showPasteDialog = false },
                title = { Text("Paste code") },
                text = {
                    OutlinedTextField(
                        value = manualPaste,
                        onValueChange = { manualPaste = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 8
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val paste = manualPaste.trimEnd()
                        if (paste.isNotBlank()) {
                            codeInput = if (codeInput.isBlank()) paste else codeInput + "\n" + paste
                        }
                        showPasteDialog = false
                        manualPaste = ""
                    }) { Text("Insert") }
                },
                dismissButton = {
                    TextButton(onClick = { showPasteDialog = false; manualPaste = "" }) { Text("Cancel") }
                }
            )
        }

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Generate button
        Button(
            onClick = { 
                if (codeInput.isNotBlank()) {
                    onGenerateFromCode(codeInput, selectedLanguage)
                }
            },
            enabled = isAiAvailable && !isGenerating && codeInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isGenerating) "Analyzing Code..." else "Generate Diagram",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Tips section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Tips for best results:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• For class diagrams: Include complete class definitions with methods and properties\n" +
                            "• For ER diagrams: Provide CREATE TABLE statements with foreign keys\n" +
                            "• For flowcharts: Include functions with clear logic flow\n" +
                            "• Code will be automatically analyzed to determine the best diagram type",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getPlaceholderText(language: CodeLanguage): String {
    return when (language) {
        CodeLanguage.KOTLIN -> """
            class User(
                val id: String,
                val name: String,
                val email: String
            ) {
                fun sendNotification(): Boolean {
                    // Implementation
                    return true
                }
            }
        """.trimIndent()
        
        CodeLanguage.JAVA -> """
            public class User {
                private String id;
                private String name;
                private String email;
                
                public boolean sendNotification() {
                    // Implementation
                    return true;
                }
            }
        """.trimIndent()
        
        CodeLanguage.SQL -> """
            CREATE TABLE users (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL
            );
            
            CREATE TABLE orders (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36),
                total DECIMAL(10,2),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """.trimIndent()
        
        CodeLanguage.JAVASCRIPT -> """
            class User {
                constructor(id, name, email) {
                    this.id = id;
                    this.name = name;
                    this.email = email;
                }
                
                sendNotification() {
                    // Implementation
                    return true;
                }
            }
        """.trimIndent()
        
        CodeLanguage.PYTHON -> """
            class User:
                def __init__(self, id, name, email):
                    self.id = id
                    self.name = name
                    self.email = email
                
                def send_notification(self):
                    # Implementation
                    return True
        """.trimIndent()
        
        else -> "Paste your code here..."
    }
}