package com.example.mermaidmaker.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mermaidmaker.data.service.ThumbnailGenerator
import com.example.mermaidmaker.domain.model.MermaidDiagram
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
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
            modifier = Modifier.padding(paddingValues),
            onDiagramClick = { diagramId -> navController.navigate("editor/$diagramId") }
        )
    }
}

@Composable
private fun HomeContent(
    diagrams: List<MermaidDiagram>,
    modifier: Modifier = Modifier,
    onDiagramClick: (String) -> Unit = {}
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
            DiagramRow(
                diagram = diagram,
                onDiagramClick = { onDiagramClick(diagram.id) }
            )
        }
    }
}

@Composable
private fun DiagramRow(
    diagram: MermaidDiagram,
    onDiagramClick: () -> Unit
) {
    val context = LocalContext.current
    val thumbnailGenerator: ThumbnailGenerator = koinInject()
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDiagramClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail preview
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(45.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val thumbnailFile = thumbnailGenerator.getThumbnailFile(diagram.thumbnailPath)
                val bitmap = remember(diagram.thumbnailPath) {
                    thumbnailFile?.let { file ->
                        try {
                            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Diagram preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "No preview",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = diagram.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = diagram.diagramType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Updated " + diagram.updatedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (diagram.isFavorite) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

