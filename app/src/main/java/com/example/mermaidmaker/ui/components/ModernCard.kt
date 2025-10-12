package com.example.mermaidmaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.common.AnimationStyle
import com.example.mermaidmaker.ui.common.BaseAnimatedCard
import com.example.mermaidmaker.ui.common.CardAction
import com.example.mermaidmaker.ui.common.CardVariant
import com.example.mermaidmaker.ui.common.HeaderStyle
import com.example.mermaidmaker.ui.common.StandardCardHeader
import com.example.mermaidmaker.ui.common.StandardDropdownMenu

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseAnimatedCard(
        modifier = modifier,
        onClick = onClick,
        enabled = true,
        variant = CardVariant.Elevated,
        animationStyle = AnimationStyle.Modern,
        content = content
    )
}

@Composable
fun ModernCardHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    StandardCardHeader(
        title = title,
        subtitle = subtitle,
        icon = icon,
        style = HeaderStyle.Modern,
        actions = actions
    )
}

@Composable
fun ModernCardActions(
    actions: List<CardAction>
) {
    StandardDropdownMenu(
        actions = actions,
        style = HeaderStyle.Modern
    )
}

@Composable
fun ModernBadge(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = color
        )
    }
}