package com.example.mermaidmaker.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Professional Design System Tokens
 * Following Material Design 3 and modern design principles
 */

// Spacing Scale
object Spacing {
    val none = 0.dp
    val xs = 4.dp      // Micro spacing
    val sm = 8.dp      // Small spacing
    val md = 12.dp     // Medium spacing  
    val lg = 16.dp     // Large spacing
    val xl = 20.dp     // Extra large spacing
    val xxl = 24.dp    // Double extra large
    val xxxl = 32.dp   // Triple extra large
    val huge = 40.dp   // Huge spacing
    val massive = 48.dp // Massive spacing
}

// Corner Radius Scale
object CornerRadius {
    val none = 0.dp
    val xs = 4.dp      // Small corners
    val sm = 6.dp      // Small-medium corners
    val md = 8.dp      // Medium corners
    val lg = 12.dp     // Large corners
    val xl = 16.dp     // Extra large corners
    val xxl = 20.dp    // Double extra large
    val xxxl = 24.dp   // Triple extra large
    val round = 50.dp  // Fully rounded
}

// Elevation Scale
object Elevation {
    val none = 0.dp
    val xs = 1.dp      // Subtle elevation
    val sm = 2.dp      // Small elevation
    val md = 4.dp      // Medium elevation
    val lg = 6.dp      // Large elevation
    val xl = 8.dp      // Extra large elevation
    val xxl = 12.dp    // Double extra large
    val xxxl = 16.dp   // Triple extra large
    val floating = 24.dp // Floating elements
}

// Border Widths
object BorderWidth {
    val none = 0.dp
    val thin = 0.5.dp
    val normal = 1.dp
    val thick = 1.5.dp
    val bold = 2.dp
}

// Icon Sizes
object IconSize {
    val xs = 12.dp
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 28.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val huge = 48.dp
    val massive = 64.dp
}

// Component Heights
object ComponentHeight {
    val button = 44.dp
    val buttonSmall = 36.dp
    val buttonLarge = 52.dp
    val textField = 48.dp
    val topBar = 64.dp
    val bottomBar = 72.dp
    val fab = 56.dp
    val fabSmall = 40.dp
    val fabLarge = 64.dp
}

// Container Sizes
object ContainerSize {
    val card = 280.dp
    val cardLarge = 320.dp
    val dialog = 400.dp
    val bottomSheet = 600.dp
}

// Animation Durations (in milliseconds)
object AnimationDuration {
    const val fast = 150
    const val normal = 300
    const val slow = 500
    const val extraSlow = 800
}

// Z-Index Layers
object ZIndex {
    const val base = 0f
    const val raised = 1f
    const val overlay = 10f
    const val modal = 20f
    const val popover = 30f
    const val tooltip = 40f
    const val toast = 50f
}