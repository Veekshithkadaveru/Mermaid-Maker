package com.example.mermaidmaker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.AnimationDuration
import com.example.mermaidmaker.ui.theme.CornerRadius
import com.example.mermaidmaker.ui.theme.Elevation
import com.example.mermaidmaker.ui.theme.IconSize
import com.example.mermaidmaker.ui.theme.OnSuccessContainer
import com.example.mermaidmaker.ui.theme.OnWarningContainer
import com.example.mermaidmaker.ui.theme.Spacing
import com.example.mermaidmaker.ui.theme.SuccessContainer
import com.example.mermaidmaker.ui.theme.WarningContainer

/**
 * Professional card components with modern design principles
 */

@Composable
fun ProfessionalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    variant: CardVariant = CardVariant.Elevated,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && onClick != null) 0.98f else 1f,
        animationSpec = tween(AnimationDuration.fast),
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = when {
            !enabled -> 0f
            isHovered && onClick != null -> getCardElevation(variant) + 2f
            else -> getCardElevation(variant)
        },
        animationSpec = tween(AnimationDuration.normal),
        label = "card_elevation"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
            isHovered && onClick != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            variant == CardVariant.Outlined -> MaterialTheme.colorScheme.outline
            else -> Color.Transparent
        },
        animationSpec = tween(AnimationDuration.fast),
        label = "card_border"
    )

    val colors = getCardColors(variant, enabled)

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
        shape = RoundedCornerShape(CornerRadius.lg)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            content = content
        )
    }
}

@Composable
fun ProfessionalCardHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
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
                Spacer(modifier = Modifier.width(Spacing.md))
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
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
fun ProfessionalCardContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        content = content
    )
}

@Composable
fun ProfessionalCardActions(
    actions: List<CardAction>,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    if (actions.isNotEmpty()) {
        Box(modifier = modifier) {
            ProfessionalIconButton(
                onClick = { showMenu = true },
                icon = Icons.Outlined.MoreVert,
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Small,
                contentDescription = "More actions"
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
                                    modifier = Modifier.size(IconSize.md)
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
fun ProfessionalBadge(
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
fun ProfessionalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness
    )
}

enum class CardVariant {
    Elevated,
    Filled,
    Outlined
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

data class CardColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)

data class BadgeColors(
    val container: Color,
    val content: Color
)

data class BadgeSizes(
    val padding: androidx.compose.foundation.layout.PaddingValues,
    val textStyle: androidx.compose.ui.text.TextStyle,
    val cornerRadius: androidx.compose.ui.unit.Dp
)

data class CardAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

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
        CardVariant.Elevated -> Elevation.sm.value
        CardVariant.Filled -> Elevation.none.value
        CardVariant.Outlined -> Elevation.none.value
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
            padding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Spacing.sm,
                vertical = Spacing.xs
            ),
            textStyle = MaterialTheme.typography.labelSmall,
            cornerRadius = CornerRadius.xs
        )
        BadgeSize.Medium -> BadgeSizes(
            padding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            ),
            textStyle = MaterialTheme.typography.labelMedium,
            cornerRadius = CornerRadius.sm
        )
        BadgeSize.Large -> BadgeSizes(
            padding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Spacing.lg,
                vertical = Spacing.md
            ),
            textStyle = MaterialTheme.typography.labelLarge,
            cornerRadius = CornerRadius.md
        )
    }
}