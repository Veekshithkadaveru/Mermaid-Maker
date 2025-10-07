package com.example.mermaidmaker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.AnimationDuration
import com.example.mermaidmaker.ui.theme.ComponentHeight
import com.example.mermaidmaker.ui.theme.CornerRadius
import com.example.mermaidmaker.ui.theme.IconSize
import com.example.mermaidmaker.ui.theme.Spacing

/**
 * Professional button variants following modern design principles
 */

@Composable
fun ProfessionalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(AnimationDuration.fast),
        label = "button_scale"
    )
    
    val buttonColors = getButtonColors(variant)
    val buttonSizes = getButtonSizes(size)
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> buttonColors.disabledContainer
            isPressed -> buttonColors.pressedContainer
            isHovered -> buttonColors.hoveredContainer
            else -> buttonColors.container
        },
        animationSpec = tween(AnimationDuration.fast),
        label = "button_background"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> buttonColors.disabledContent
            else -> buttonColors.content
        },
        animationSpec = tween(AnimationDuration.fast),
        label = "button_content"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = buttonSizes.height),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(buttonSizes.cornerRadius),
        color = backgroundColor,
        contentColor = contentColor,
        border = if (variant == ButtonVariant.Outlined) {
            BorderStroke(1.dp, if (enabled) buttonColors.outline else buttonColors.disabledOutline)
        } else null,
        shadowElevation = buttonSizes.elevation,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(value = buttonSizes.textStyle) {
                Row(
                    modifier = Modifier.padding(buttonSizes.contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(buttonSizes.iconSize),
                                strokeWidth = 2.dp,
                                color = contentColor
                            )
                            if (leadingIcon != null || trailingIcon != null) {
                                Spacer(modifier = Modifier.width(Spacing.sm))
                            }
                        }
                        leadingIcon != null -> {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(buttonSizes.iconSize)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                        }
                    }
                    
                    content()
                    
                    if (trailingIcon != null && !loading) {
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(buttonSizes.iconSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Ghost,
    size: ButtonSize = ButtonSize.Medium,
    contentDescription: String? = null
) {
    val buttonSizes = getButtonSizes(size)
    val iconButtonSize = buttonSizes.height
    
    ProfessionalButton(
        onClick = onClick,
        modifier = modifier.size(iconButtonSize),
        enabled = enabled,
        variant = variant,
        size = size
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(buttonSizes.iconSize)
        )
    }
}

@Composable
fun ProfessionalFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    expanded: Boolean = false,
    label: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed && enabled -> 0.92f
            isHovered && enabled -> 1.05f
            else -> 1f
        },
        animationSpec = tween(AnimationDuration.normal),
        label = "fab_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outline
            isPressed -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(AnimationDuration.fast),
        label = "fab_background"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = if (expanded && label != null) {
            RoundedCornerShape(CornerRadius.xl)
        } else {
            RoundedCornerShape(CornerRadius.xl)
        },
        color = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = if (isHovered) 8.dp else 6.dp,
        interactionSource = interactionSource
    ) {
        if (expanded && label != null) {
            Row(
                modifier = Modifier.padding(
                    horizontal = Spacing.lg,
                    vertical = Spacing.md
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.lg)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(ComponentHeight.fab)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(IconSize.lg)
                )
            }
        }
    }
}

enum class ButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Text,
    Ghost
}

enum class ButtonSize {
    Small,
    Medium,
    Large
}

data class ButtonColors(
    val container: Color,
    val content: Color,
    val hoveredContainer: Color,
    val pressedContainer: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val outline: Color = Color.Transparent,
    val disabledOutline: Color = Color.Transparent
)

data class ButtonSizes(
    val height: androidx.compose.ui.unit.Dp,
    val contentPadding: PaddingValues,
    val iconSize: androidx.compose.ui.unit.Dp,
    val textStyle: androidx.compose.ui.text.TextStyle,
    val cornerRadius: androidx.compose.ui.unit.Dp,
    val elevation: androidx.compose.ui.unit.Dp
)

@Composable
private fun getButtonColors(variant: ButtonVariant): ButtonColors {
    return when (variant) {
        ButtonVariant.Primary -> ButtonColors(
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            hoveredContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            pressedContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            disabledContainer = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ButtonVariant.Secondary -> ButtonColors(
            container = MaterialTheme.colorScheme.secondary,
            content = MaterialTheme.colorScheme.onSecondary,
            hoveredContainer = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
            pressedContainer = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            disabledContainer = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ButtonVariant.Outlined -> ButtonColors(
            container = Color.Transparent,
            content = MaterialTheme.colorScheme.primary,
            hoveredContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            pressedContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            disabledContainer = Color.Transparent,
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            outline = MaterialTheme.colorScheme.outline,
            disabledOutline = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
        ButtonVariant.Text -> ButtonColors(
            container = Color.Transparent,
            content = MaterialTheme.colorScheme.primary,
            hoveredContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            pressedContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            disabledContainer = Color.Transparent,
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ButtonVariant.Ghost -> ButtonColors(
            container = Color.Transparent,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            hoveredContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            pressedContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            disabledContainer = Color.Transparent,
            disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun getButtonSizes(size: ButtonSize): ButtonSizes {
    return when (size) {
        ButtonSize.Small -> ButtonSizes(
            height = ComponentHeight.buttonSmall,
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
            iconSize = IconSize.sm,
            textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            cornerRadius = CornerRadius.sm,
            elevation = 1.dp
        )
        ButtonSize.Medium -> ButtonSizes(
            height = ComponentHeight.button,
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
            iconSize = IconSize.md,
            textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            cornerRadius = CornerRadius.md,
            elevation = 2.dp
        )
        ButtonSize.Large -> ButtonSizes(
            height = ComponentHeight.buttonLarge,
            contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.lg),
            iconSize = IconSize.lg,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            cornerRadius = CornerRadius.lg,
            elevation = 3.dp
        )
    }
}