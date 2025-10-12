package com.example.mermaidmaker.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.AnimationDuration
import com.example.mermaidmaker.ui.theme.CornerRadius
import com.example.mermaidmaker.ui.theme.IconSize
import com.example.mermaidmaker.ui.theme.OnSuccessContainer
import com.example.mermaidmaker.ui.theme.OnWarningContainer
import com.example.mermaidmaker.ui.theme.Spacing
import com.example.mermaidmaker.ui.theme.SuccessContainer
import com.example.mermaidmaker.ui.theme.WarningContainer

data class CardAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

enum class HeaderStyle {
    Professional,
    Modern
}

enum class BadgeVariant {
    Default,
    Primary,
    Secondary,
    Success,
    Warning,
    Error
}

enum class BadgeSize {
    Small,
    Medium,
    Large
}

data class BadgeColors(
    val container: Color,
    val content: Color
)

data class BadgeSizes(
    val padding: PaddingValues,
    val textStyle: TextStyle,
    val cornerRadius: Dp
)

@Composable
fun Modifier.animatedInteraction(
    enabled: Boolean = true,
    scalePressed: Float = 0.96f,
    scaleHovered: Float = 1f,
    elevationPressed: Float = 0f,
    elevationHovered: Float = 2f,
    animationStyle: AnimationStyle = AnimationStyle.Professional
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f
            isPressed -> scalePressed
            isHovered -> scaleHovered
            else -> 1f
        },
        animationSpec = when (animationStyle) {
            AnimationStyle.Professional -> tween(AnimationDuration.fast)
            AnimationStyle.Modern -> spring()
        },
        label = "interaction_scale"
    )
    
    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) { }
}

@Composable
fun StandardIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = IconSize.md,
    enabled: Boolean = true,
    style: HeaderStyle = HeaderStyle.Professional
) {
    when (style) {
        HeaderStyle.Professional -> {
            // Use existing ProfessionalIconButton pattern
            Surface(
                modifier = modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(CornerRadius.sm)),
                color = if (enabled) MaterialTheme.colorScheme.surfaceVariant 
                       else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                onClick = onClick
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(size),
                        tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
        }
        HeaderStyle.Modern -> {
            IconButton(
                onClick = onClick,
                modifier = modifier.size(32.dp),
                enabled = enabled
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}

@Composable
fun StandardCardHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    style: HeaderStyle = HeaderStyle.Professional,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                when (style) {
                    HeaderStyle.Professional -> {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(CornerRadius.md)),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(IconSize.md)
                                )
                            }
                        }
                    }
                    HeaderStyle.Modern -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(when (style) {
                    HeaderStyle.Professional -> Spacing.md
                    HeaderStyle.Modern -> 12.dp
                }))
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = when (style) {
                        HeaderStyle.Professional -> 2
                        HeaderStyle.Modern -> 1
                    },
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    if (style == HeaderStyle.Professional) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Row(content = actions)
    }
}

@Composable
fun StandardDropdownMenu(
    actions: List<CardAction>,
    modifier: Modifier = Modifier,
    style: HeaderStyle = HeaderStyle.Professional
) {
    var showMenu by remember { mutableStateOf(false) }
    
    if (actions.isNotEmpty()) {
        Box(modifier = modifier) {
            StandardIconButton(
                icon = when (style) {
                    HeaderStyle.Professional -> Icons.Outlined.MoreVert
                    HeaderStyle.Modern -> Icons.Outlined.MoreHoriz
                },
                contentDescription = "More actions",
                onClick = { showMenu = true },
                style = style
            )
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                actions.forEach { action ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            action.onClick()
                            showMenu = false
                        },
                        leadingIcon = if (action.icon != null) {
                            {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(when (style) {
                                        HeaderStyle.Professional -> IconSize.md
                                        HeaderStyle.Modern -> 18.dp
                                    })
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun StandardBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    size: BadgeSize = BadgeSize.Medium
) {
    val colors = getBadgeColors(variant)
    val sizes = getBadgeSize(size)
    
    Surface(
        modifier = modifier.clip(RoundedCornerShape(sizes.cornerRadius)),
        color = colors.container,
        contentColor = colors.content
    ) {
        Text(
            text = text,
            style = sizes.textStyle.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(sizes.padding)
        )
    }
}

@Composable
private fun getBadgeColors(variant: BadgeVariant): BadgeColors {
    return when (variant) {
        BadgeVariant.Default -> BadgeColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BadgeVariant.Primary -> BadgeColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer
        )
        BadgeVariant.Secondary -> BadgeColors(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer
        )
        BadgeVariant.Success -> BadgeColors(
            container = SuccessContainer,
            content = OnSuccessContainer
        )
        BadgeVariant.Warning -> BadgeColors(
            container = WarningContainer,
            content = OnWarningContainer
        )
        BadgeVariant.Error -> BadgeColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun getBadgeSize(size: BadgeSize): BadgeSizes {
    return when (size) {
        BadgeSize.Small -> BadgeSizes(
            padding = PaddingValues(
                horizontal = Spacing.sm,
                vertical = Spacing.xs
            ),
            textStyle = MaterialTheme.typography.labelSmall,
            cornerRadius = CornerRadius.xs
        )
        BadgeSize.Medium -> BadgeSizes(
            padding = PaddingValues(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            ),
            textStyle = MaterialTheme.typography.labelMedium,
            cornerRadius = CornerRadius.sm
        )
        BadgeSize.Large -> BadgeSizes(
            padding = PaddingValues(
                horizontal = Spacing.lg,
                vertical = Spacing.md
            ),
            textStyle = MaterialTheme.typography.labelLarge,
            cornerRadius = CornerRadius.md
        )
    }
}