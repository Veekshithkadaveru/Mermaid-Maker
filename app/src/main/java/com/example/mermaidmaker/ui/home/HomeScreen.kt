package com.example.mermaidmaker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mermaidmaker.domain.model.MermaidDiagram
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val diagrams by viewModel.diagrams.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create new diagram")
            }
        }
    ) { paddingValues ->
        HomeContent(
            diagrams = diagrams,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeContent(
    diagrams: List<MermaidDiagram>,
    modifier: Modifier = Modifier
) {
    if (diagrams.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No diagrams yet",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Tap + to create your first diagram",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(diagrams, key = { it.id }) { diagram ->
            DiagramRow(diagram = diagram)
        }
    }
}

@Composable
private fun DiagramRow(diagram: MermaidDiagram) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: navigate to detail/editor */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = diagram.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Updated " + diagram.updatedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

