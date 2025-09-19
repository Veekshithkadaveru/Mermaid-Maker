package com.example.mermaidmaker.ui.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import com.example.mermaidmaker.domain.model.DiagramType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGenerationTab(
    selectedDiagramType: DiagramType,
    onDiagramTypeSelected: (DiagramType) -> Unit,
    onGenerateClick: (String, DiagramType) -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit = {},
    isGenerating: Boolean = false,
    errorMessage: String? = null,
    isAiAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    var userPrompt by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header
        Text(
            text = "AI Diagram Generator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Describe your diagram in plain English and let AI create the Mermaid code for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isAiAvailable) {
            // API key setup prompt
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "API Key Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Text(
                        text = "To use AI features, you need to add your OpenAI or Google Gemini API key in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSettingsClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Settings")
                        }

                        OutlinedButton(
                            onClick = onRefreshClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }
        } else {
            // Diagram type selector
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = getDiagramTypeDisplayName(selectedDiagramType),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Diagram Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    DiagramType.values().forEach { diagramType ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = getDiagramTypeDisplayName(diagramType),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = getDiagramTypeDescription(diagramType),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onDiagramTypeSelected(diagramType)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Text input area
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("Describe your diagram") },
                placeholder = {
                    Text(getPlaceholderForDiagramType(selectedDiagramType))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6,
                enabled = !isGenerating
            )

            // Generate button with enhanced loading
            Button(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        onGenerateClick(userPrompt.trim(), selectedDiagramType)
                    }
                },
                enabled = userPrompt.isNotBlank() && !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Generating diagram...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Generate Diagram")
                }
            }

            // Note: Full-screen loading overlay is handled in MainEditorScreen

            // Error message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Examples section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Example prompts:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    getExamplesForDiagramType(selectedDiagramType).forEach { example ->
                        TextButton(
                            onClick = { userPrompt = example },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"$example\"",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDiagramTypeDisplayName(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Flowchart"
        DiagramType.SEQUENCE -> "Sequence Diagram"
        DiagramType.CLASS -> "Class Diagram"
        DiagramType.STATE -> "State Diagram"
        DiagramType.ER_DIAGRAM -> "ER Diagram"
        DiagramType.GANTT -> "Gantt Chart"
        DiagramType.PIE -> "Pie Chart"
        DiagramType.GITGRAPH -> "Git Graph"
        DiagramType.JOURNEY -> "User Journey"
    }
}

private fun getDiagramTypeDescription(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Process flows and decision trees"
        DiagramType.SEQUENCE -> "Interactions between entities over time"
        DiagramType.CLASS -> "Object-oriented class relationships"
        DiagramType.STATE -> "State machines and transitions"
        DiagramType.ER_DIAGRAM -> "Database entity relationships"
        DiagramType.GANTT -> "Project timelines and schedules"
        DiagramType.PIE -> "Data proportions and percentages"
        DiagramType.GITGRAPH -> "Git workflow and branching"
        DiagramType.JOURNEY -> "User experience and journey maps"
    }
}

private fun getPlaceholderForDiagramType(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "e.g., Show the login process for a mobile app"
        DiagramType.SEQUENCE -> "e.g., API call flow between client and server"
        DiagramType.CLASS -> "e.g., E-commerce system with products and orders"
        DiagramType.STATE -> "e.g., User session states from login to logout"
        DiagramType.ER_DIAGRAM -> "e.g., Blog database with users, posts, and comments"
        DiagramType.GANTT -> "e.g., Website development project timeline"
        DiagramType.PIE -> "e.g., Company revenue breakdown by department"
        DiagramType.GITGRAPH -> "e.g., Feature branch workflow with main and develop"
        DiagramType.JOURNEY -> "e.g., Customer journey from discovery to purchase"
    }
}

private fun getExamplesForDiagramType(diagramType: DiagramType): List<String> {
    return when (diagramType) {
        DiagramType.FLOWCHART -> listOf(
            "Show the login process for a mobile app",
            "Create a flowchart for password reset",
            "Show the checkout process for an e-commerce site"
        )

        DiagramType.SEQUENCE -> listOf(
            "API call flow between client and server",
            "User authentication process",
            "Payment processing workflow"
        )

        DiagramType.CLASS -> listOf(
            "E-commerce system with products and orders",
            "Library management system",
            "Social media platform structure"
        )

        DiagramType.STATE -> listOf(
            "User session states from login to logout",
            "Order processing states",
            "Media player states"
        )

        DiagramType.ER_DIAGRAM -> listOf(
            "Blog database with users, posts, and comments",
            "E-commerce database structure",
            "School management system database"
        )

        DiagramType.GANTT -> listOf(
            "Website development project timeline",
            "Marketing campaign schedule",
            "Software release plan"
        )

        DiagramType.PIE -> listOf(
            "Company revenue breakdown by department",
            "Website traffic sources",
            "Project time allocation"
        )

        DiagramType.GITGRAPH -> listOf(
            "Feature branch workflow with main and develop",
            "Hotfix deployment process",
            "Release branching strategy"
        )

        DiagramType.JOURNEY -> listOf(
            "Customer journey from discovery to purchase",
            "User onboarding experience",
            "Support ticket resolution process"
        )
    }
}

@Composable
private fun ProfessionalLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Multiple sophisticated animation values
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )
    
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    // Dynamic gradient background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.1f),
                        secondaryColor.copy(alpha = 0.15f),
                        tertiaryColor.copy(alpha = 0.1f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f * gradientShift, 500f * gradientShift)
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sophisticated particle animation
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                AdvancedLoadingIndicator(
                    rotationAngle = rotationAngle,
                    pulseScale = pulseScale,
                    gradientShift = gradientShift
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🧠 AI Thinking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(textAlpha)
                )
                Text(
                    text = "Crafting your perfect diagram...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textAlpha * 0.9f)
                )
                Text(
                    text = "Analyzing • Processing • Generating",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textAlpha * 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AdvancedLoadingIndicator(
    rotationAngle: Float,
    pulseScale: Float,
    gradientShift: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(
        modifier = Modifier.size(100.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 6
        
        // Outer rotating ring with gradient
        rotate(rotationAngle, center) {
            for (i in 0 until 8) {
                val angle = i * 45f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 2.5f * pulseScale
                val particleCenter = Offset(
                    center.x + cos(rad).toFloat() * distance,
                    center.y + sin(rad).toFloat() * distance
                )
                
                val alpha = (sin(Math.toRadians((rotationAngle + angle).toDouble())).toFloat() + 1f) / 2f
                drawCircle(
                    color = primaryColor.copy(alpha = alpha * 0.8f),
                    radius = baseRadius * 0.3f * (1f + alpha * 0.5f),
                    center = particleCenter
                )
            }
        }
        
        // Middle pulsing ring
        rotate(-rotationAngle * 0.7f, center) {
            for (i in 0 until 6) {
                val angle = i * 60f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 1.8f
                val particleCenter = Offset(
                    center.x + cos(rad).toFloat() * distance,
                    center.y + sin(rad).toFloat() * distance
                )
                
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.6f),
                    radius = baseRadius * 0.25f * pulseScale,
                    center = particleCenter
                )
            }
        }
        
        // Inner core with gradient
        val gradientColors = listOf(
            primaryColor.copy(alpha = 0.9f),
            secondaryColor.copy(alpha = 0.7f),
            tertiaryColor.copy(alpha = 0.5f)
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = gradientColors,
                radius = baseRadius * pulseScale
            ),
            radius = baseRadius * pulseScale,
            center = center
        )
        
        // Central highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = baseRadius * 0.4f * pulseScale,
            center = center
        )
    }
}