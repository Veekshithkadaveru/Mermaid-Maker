package com.example.mermaidmaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.common.AnimationStyle
import com.example.mermaidmaker.ui.common.BadgeSize
import com.example.mermaidmaker.ui.common.BadgeVariant
import com.example.mermaidmaker.ui.common.BaseAnimatedCard
import com.example.mermaidmaker.ui.common.CardAction
import com.example.mermaidmaker.ui.common.CardVariant
import com.example.mermaidmaker.ui.common.HeaderStyle
import com.example.mermaidmaker.ui.common.StandardBadge
import com.example.mermaidmaker.ui.common.StandardCardHeader
import com.example.mermaidmaker.ui.common.StandardDropdownMenu
import com.example.mermaidmaker.ui.theme.Spacing

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
    BaseAnimatedCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        animationStyle = AnimationStyle.Professional,
        content = content
    )
}

@Composable
fun ProfessionalCardHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    StandardCardHeader(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = modifier,
        style = HeaderStyle.Professional,
        actions = actions
    )
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
    StandardDropdownMenu(
        actions = actions,
        modifier = modifier,
        style = HeaderStyle.Professional
    )
}

@Composable
fun ProfessionalBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    size: BadgeSize = BadgeSize.Medium
) {
    StandardBadge(
        text = text,
        modifier = modifier,
        variant = variant,
        size = size
    )
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

