package com.example.mermaidmaker.ui.editor.validation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Professional error panel displaying validation errors with filtering and details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorPanel(
    validationResult: ValidationResult,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onErrorClick: (MermaidValidationError) -> Unit,
    onQuickFix: (MermaidValidationError) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var filterSeverity by remember { mutableStateOf<ValidationSeverity?>(null) }
    var filterCategory by remember { mutableStateOf<String?>(null) }
    
    val filteredErrors = remember(validationResult, filterSeverity, filterCategory) {
        validationResult.errors.filter { error ->
            (filterSeverity == null || error.severity == filterSeverity) &&
            (filterCategory == null || error.type.category == filterCategory)
        }
    }
    
    val categories = remember(validationResult) {
        validationResult.errors.map { it.type.category }.distinct().sorted()
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column {
            // Header with summary and toggle
            ErrorPanelHeader(
                validationResult = validationResult,
                isExpanded = isExpanded,
                onToggleExpanded = onToggleExpanded
            )
            
            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    // Filters
                    if (validationResult.errors.isNotEmpty()) {
                        ErrorFilters(
                            filterSeverity = filterSeverity,
                            filterCategory = filterCategory,
                            categories = categories,
                            onSeverityFilter = { filterSeverity = it },
                            onCategoryFilter = { filterCategory = it },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    // Error list
                    if (filteredErrors.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredErrors) { error ->
                                ErrorItem(
                                    error = error,
                                    onClick = { onErrorClick(error) },
                                    onQuickFix = if (error.quickFix != null) { { onQuickFix(error) } } else null
                                )
                            }
                        }
                    } else if (validationResult.errors.isNotEmpty()) {
                        // No errors match filter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No errors match the current filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header section with error summary and expand/collapse toggle
 */
@Composable
private fun ErrorPanelHeader(
    validationResult: ValidationResult,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        color = if (validationResult.errorCount > 0) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        } else if (validationResult.warningCount > 0) {
            Color(0xFFFFF3E0) // Light orange
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status icon
                Icon(
                    imageVector = when {
                        validationResult.errorCount > 0 -> Icons.Default.Error
                        validationResult.warningCount > 0 -> Icons.Default.Warning
                        validationResult.infoCount > 0 -> Icons.Default.Info
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when {
                        validationResult.errorCount > 0 -> MaterialTheme.colorScheme.error
                        validationResult.warningCount > 0 -> Color(0xFFF57C00)
                        validationResult.infoCount > 0 -> MaterialTheme.colorScheme.primary
                        else -> Color(0xFF4CAF50)
                    }
                )
                
                Column {
                    Text(
                        text = validationResult.getSummaryText(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (validationResult.totalCount > 0) {
                        Text(
                            text = "Click to view details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Expand/collapse icon
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Filter controls for severity and category
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorFilters(
    filterSeverity: ValidationSeverity?,
    filterCategory: String?,
    categories: List<String>,
    onSeverityFilter: (ValidationSeverity?) -> Unit,
    onCategoryFilter: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Severity filter
        FilterChipGroup(
            title = "Severity",
            items = ValidationSeverity.values().toList(),
            selectedItem = filterSeverity,
            onSelectionChanged = onSeverityFilter,
            itemLabel = { it.displayName },
            modifier = Modifier.weight(1f)
        )
        
        // Category filter
        FilterChipGroup(
            title = "Category",
            items = categories,
            selectedItem = filterCategory,
            onSelectionChanged = onCategoryFilter,
            itemLabel = { it },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Generic filter chip group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterChipGroup(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onSelectionChanged: (T?) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedItem == null,
                onClick = { onSelectionChanged(null) },
                label = { Text("All", style = MaterialTheme.typography.labelSmall) }
            )
            
            // Individual item chips
            items.take(3).forEach { item ->
                FilterChip(
                    selected = selectedItem == item,
                    onClick = { 
                        onSelectionChanged(if (selectedItem == item) null else item) 
                    },
                    label = { 
                        Text(
                            itemLabel(item), 
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    }
                )
            }
        }
    }
}

/**
 * Individual error item display
 */
@Composable
private fun ErrorItem(
    error: MermaidValidationError,
    onClick: () -> Unit,
    onQuickFix: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header with severity, category, and line number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Severity badge
                    SeverityBadge(error.severity)
                    
                    // Category
                    Text(
                        text = error.type.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Line number
                Text(
                    text = "Line ${error.line + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Error message
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Suggestion
            error.suggestion?.let { suggestion ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Quick fix button
            onQuickFix?.let {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = it,
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Quick Fix",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Severity badge component
 */
@Composable
private fun SeverityBadge(severity: ValidationSeverity) {
    val backgroundColor = when (severity) {
        ValidationSeverity.ERROR -> MaterialTheme.colorScheme.error
        ValidationSeverity.WARNING -> Color(0xFFF57C00)
        ValidationSeverity.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val icon = when (severity) {
        ValidationSeverity.ERROR -> Icons.Default.Error
        ValidationSeverity.WARNING -> Icons.Default.Warning
        ValidationSeverity.INFO -> Icons.Default.Info
    }
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = backgroundColor
        )
        Text(
            text = severity.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor,
            fontSize = 10.sp
        )
    }
}