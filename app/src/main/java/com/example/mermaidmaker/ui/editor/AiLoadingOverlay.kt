package com.example.mermaidmaker.ui.editor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FullScreenAiLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_loading")

    // Advanced animation values for full-screen experience
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_alpha"
    )

    val textGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_glow"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = backgroundAlpha),
                        primaryColor.copy(alpha = backgroundAlpha * 0.1f),
                        secondaryColor.copy(alpha = backgroundAlpha * 0.05f)
                    ),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Main loading indicator - larger for full screen
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                FullScreenLoadingIndicator(
                    rotationAngle = rotationAngle,
                    pulseScale = pulseScale
                )
            }

            // Professional branding and messaging
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.alpha(textGlow)
                )

                Text(
                    text = "AI THINKING",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 4.sp,
                    modifier = Modifier.alpha(textGlow)
                )

                Text(
                    text = "Crafting your perfect diagram",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alpha(textGlow * 0.9f)
                )

                Spacer(modifier = androidx.compose.ui.Modifier.size(8.dp))

                Text(
                    text = "ANALYZING • PROCESSING • GENERATING",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(textGlow * 0.8f)
                )

                Spacer(modifier = androidx.compose.ui.Modifier.size(16.dp))

                Text(
                    text = "Please wait while our AI creates your diagram...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textGlow * 0.7f)
                )
            }
        }
    }
}

@Composable
fun FullScreenLoadingIndicator(
    rotationAngle: Float,
    pulseScale: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 8

        // Outer orbital ring with multiple particles
        rotate(rotationAngle, center) {
            for (i in 0 until 12) {
                val angle = i * 30f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 4f * pulseScale
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )

                val alpha = (kotlin.math.sin(Math.toRadians((rotationAngle + angle).toDouble()))
                    .toFloat() + 1f) / 2f
                drawCircle(
                    color = primaryColor.copy(alpha = alpha * 0.9f),
                    radius = baseRadius * 0.4f * (0.8f + alpha * 0.4f),
                    center = particleCenter
                )
            }
        }

        // Middle ring counter-rotating
        rotate(-rotationAngle * 0.6f, center) {
            for (i in 0 until 8) {
                val angle = i * 45f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 2.8f
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )

                drawCircle(
                    color = secondaryColor.copy(alpha = 0.8f),
                    radius = baseRadius * 0.35f * pulseScale,
                    center = particleCenter
                )
            }
        }

        // Inner ring with faster rotation
        rotate(rotationAngle * 1.5f, center) {
            for (i in 0 until 6) {
                val angle = i * 60f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 1.8f
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )

                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.7f),
                    radius = baseRadius * 0.25f * pulseScale,
                    center = particleCenter
                )
            }
        }

        // Central core with radial gradient
        val gradientColors = listOf(
            primaryColor.copy(alpha = 1f),
            secondaryColor.copy(alpha = 0.8f),
            tertiaryColor.copy(alpha = 0.6f),
            Color.Transparent
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = gradientColors,
                radius = baseRadius * 1.5f * pulseScale
            ),
            radius = baseRadius * 1.2f * pulseScale,
            center = center
        )

        // Central bright highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = baseRadius * 0.6f * pulseScale,
            center = center
        )

        // Very center dot
        drawCircle(
            color = primaryColor.copy(alpha = 0.9f),
            radius = baseRadius * 0.3f,
            center = center
        )
    }
}


