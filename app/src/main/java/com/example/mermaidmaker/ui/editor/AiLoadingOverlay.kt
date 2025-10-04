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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import android.provider.Settings

@Composable
fun FullScreenAiLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_loading")

    val context = LocalContext.current
    val reduceMotion = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    // Advanced animation values for full-screen experience
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val animatedPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val animatedBgAlpha by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 0.985f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_alpha"
    )
    val animatedTextGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_glow"
    )

    val rotationAngle = if (reduceMotion) 0f else animatedRotation
    val pulseScale = if (reduceMotion) 1f else animatedPulse
    val backgroundAlpha = if (reduceMotion) 0.97f else animatedBgAlpha
    val textGlow = if (reduceMotion) 1f else animatedTextGlow

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
	val surfaceColor = MaterialTheme.colorScheme.surface

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.radialGradient(
					colors = listOf(
						surfaceColor.copy(alpha = backgroundAlpha),
						primaryColor.copy(alpha = backgroundAlpha * 0.06f),
						secondaryColor.copy(alpha = backgroundAlpha * 0.03f)
					),
					radius = 1500f
				)
			)
			.semantics {
				contentDescription = "Generating your diagram"
				liveRegion = LiveRegionMode.Polite
			},
		contentAlignment = Alignment.Center
	) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Main loading indicator - larger for full screen
			Box(
				modifier = Modifier.size(144.dp),
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
					text = "Generating your diagram…",
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.Medium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					modifier = Modifier.alpha(textGlow)
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
		modifier = Modifier.size(144.dp)
	) {
		val center = Offset(size.width / 2, size.height / 2)
		val baseRadius = size.minDimension / 9

		// Outer orbital ring with multiple particles
		rotate(rotationAngle, center) {
			for (i in 0 until 8) {
				val angle = i * 30f
				val rad = Math.toRadians(angle.toDouble())
				val distance = baseRadius * 3.6f * pulseScale
				val particleCenter = Offset(
					center.x + kotlin.math.cos(rad).toFloat() * distance,
					center.y + kotlin.math.sin(rad).toFloat() * distance
				)

				val alpha = (kotlin.math.sin(Math.toRadians((rotationAngle + angle).toDouble()))
					.toFloat() + 1f) / 2f
				drawCircle(
					color = primaryColor.copy(alpha = alpha * 0.6f),
					radius = baseRadius * 0.35f * (0.85f + alpha * 0.3f),
					center = particleCenter
				)
			}
		}

		// Middle ring counter-rotating
		rotate(-rotationAngle * 0.6f, center) {
			for (i in 0 until 6) {
				val angle = i * 45f
				val rad = Math.toRadians(angle.toDouble())
				val distance = baseRadius * 2.8f
				val particleCenter = Offset(
					center.x + kotlin.math.cos(rad).toFloat() * distance,
					center.y + kotlin.math.sin(rad).toFloat() * distance
				)

				drawCircle(
					color = secondaryColor.copy(alpha = 0.5f),
					radius = baseRadius * 0.3f * pulseScale,
					center = particleCenter
				)
			}
		}

		// Inner ring with faster rotation
		rotate(rotationAngle * 1.5f, center) {
			for (i in 0 until 4) {
				val angle = i * 60f
				val rad = Math.toRadians(angle.toDouble())
				val distance = baseRadius * 1.8f
				val particleCenter = Offset(
					center.x + kotlin.math.cos(rad).toFloat() * distance,
					center.y + kotlin.math.sin(rad).toFloat() * distance
				)

				drawCircle(
					color = tertiaryColor.copy(alpha = 0.45f),
					radius = baseRadius * 0.22f * pulseScale,
					center = particleCenter
				)
			}
		}

		// Central core with radial gradient
		val gradientColors = listOf(
			primaryColor.copy(alpha = 0.9f),
			secondaryColor.copy(alpha = 0.5f),
			tertiaryColor.copy(alpha = 0.35f),
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
			color = Color.White.copy(alpha = 0.25f),
			radius = baseRadius * 0.6f * pulseScale,
			center = center
		)

		// Very center dot
		drawCircle(
			color = primaryColor.copy(alpha = 0.9f),
			radius = baseRadius * 0.25f,
			center = center
		)
	}
}


