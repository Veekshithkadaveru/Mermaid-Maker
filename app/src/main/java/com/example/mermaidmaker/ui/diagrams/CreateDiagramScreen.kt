package com.example.mermaidmaker.ui.diagrams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mermaidmaker.domain.model.DiagramType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiagramScreen(
    navController: NavController,
    viewModel: CreateDiagramViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val isSaving by viewModel.isSaving.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "New Diagram", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                modifier = Modifier.fillMaxWidth().weight(1f),
                value = content,
                onValueChange = { content = it },
                label = { Text("Mermaid content") },
                placeholder = { Text("graph TD\n    A[Start] --> B[Process]\n    B --> C[End]") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                enabled = title.isNotBlank() && content.isNotBlank() && !isSaving,
                onClick = {
                    viewModel.create(
                        title = title,
                        content = content,
                        type = DiagramType.FLOWCHART,
                        onSaved = { 
                            navController.popBackStack()
                        },
                        onError = { /* TODO: show snackbar */ }
                    )
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Create")
                }
            }
        }
    }
}

