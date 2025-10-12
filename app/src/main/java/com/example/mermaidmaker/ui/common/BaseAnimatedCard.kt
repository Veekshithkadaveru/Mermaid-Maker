package com.example.mermaidmaker.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.AnimationDuration
import com.example.mermaidmaker.ui.theme.CornerRadius
import com.example.mermaidmaker.ui.theme.Spacing

enum class CardVariant {
    Elevated,
    Filled,
    Outlined
}

enum class AnimationStyle {
    Professional, // Uses tween animations
    Modern       // Uses spring animations
}

data class CardColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)

@Composable
fun BaseAnimatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    variant: CardVariant = CardVariant.Elevated,
    animationStyle: AnimationStyle = AnimationStyle.Professional,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f
            isPressed && onClick != null -> when (animationStyle) {
                AnimationStyle.Professional -> 0.98f
                AnimationStyle.Modern -> 0.98f
            }
            isHovered && onClick != null && animationStyle == AnimationStyle.Modern -> 1.02f
            else -> 1f
        },
        animationSpec = when (animationStyle) {
            AnimationStyle.Professional -> tween(AnimationDuration.fast)
            AnimationStyle.Modern -> spring()
        },
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = when {
            !enabled -> 0f
            isHovered && onClick != null -> getCardElevation(variant) + when (animationStyle) {
                AnimationStyle.Professional -> 2f
                AnimationStyle.Modern -> 6f
            }
            else -> getCardElevation(variant)
        },
        animationSpec = when (animationStyle) {
            AnimationStyle.Professional -> tween(AnimationDuration.normal)
            AnimationStyle.Modern -> spring()
        },
        label = "card_elevation"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
            isHovered && onClick != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            variant == CardVariant.Outlined -> MaterialTheme.colorScheme.outline
            else -> Color.Transparent
        },
        animationSpec = when (animationStyle) {
            AnimationStyle.Professional -> tween(AnimationDuration.fast)
            AnimationStyle.Modern -> spring()
        },
        label = "card_border"
    )

    val colors = getCardColors(variant, enabled)
    val cornerRadius = when (animationStyle) {
        AnimationStyle.Professional -> CornerRadius.lg
        AnimationStyle.Modern -> 16.dp
    }
    val contentPadding = when (animationStyle) {
        AnimationStyle.Professional -> Spacing.xl
        AnimationStyle.Modern -> 20.dp
    }

    Card(
        modifier = modifier
            .scale(scale)
            .let { mod ->
                if (onClick != null && enabled) {
                    mod.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else mod
            },
        colors = CardDefaults.cardColors(
            containerColor = colors.container,
            contentColor = colors.content,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        ),
        border = if (variant == CardVariant.Outlined || borderColor != Color.Transparent) {
            BorderStroke(1.dp, borderColor)
        } else null,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
private fun getCardColors(variant: CardVariant, enabled: Boolean): CardColors {
    return when (variant) {
        CardVariant.Elevated -> CardColors(
            container = MaterialTheme.colorScheme.surface,
            content = MaterialTheme.colorScheme.onSurface,
            disabledContainer = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        CardVariant.Filled -> CardColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            disabledContent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
        CardVariant.Outlined -> CardColors(
            container = MaterialTheme.colorScheme.surface,
            content = MaterialTheme.colorScheme.onSurface,
            disabledContainer = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

private fun getCardElevation(variant: CardVariant): Float {
    return when (variant) {
        CardVariant.Elevated -> 2f
        CardVariant.Filled -> 0f
        CardVariant.Outlined -> 0f
    }
}