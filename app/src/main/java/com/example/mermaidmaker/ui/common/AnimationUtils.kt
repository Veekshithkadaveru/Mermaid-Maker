package com.example.mermaidmaker.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.slideInFromBottom(
    visible: Boolean,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
): Modifier {
    val density = LocalDensity.current
    val animatable = remember { Animatable(if (visible) 0f else 1f) }
    
    LaunchedEffect(visible) {
        animatable.animateTo(
            targetValue = if (visible) 0f else 1f,
            animationSpec = animationSpec
        )
    }
    
    return this.offset(y = with(density) { (animatable.value * 50.dp.toPx()).toDp() })
        .alpha(1f - animatable.value)
}

@Composable
fun Modifier.fadeInScale(
    visible: Boolean,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
): Modifier {
    val animatable = remember { Animatable(if (visible) 1f else 0f) }
    
    LaunchedEffect(visible) {
        animatable.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = animationSpec
        )
    }
    
    return this.scale(0.8f + (0.2f * animatable.value))
        .alpha(animatable.value)
}

@Composable
fun Modifier.pulseAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    return this.scale(scale)
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    return this.alpha(alpha)
}

@Composable
fun Modifier.bounceScale(
    pressed: Boolean,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
): Modifier {
    val animatable = remember { Animatable(1f) }
    
    LaunchedEffect(pressed) {
        animatable.animateTo(
            targetValue = if (pressed) 0.95f else 1f,
            animationSpec = animationSpec
        )
    }
    
    return this.scale(animatable.value)
}