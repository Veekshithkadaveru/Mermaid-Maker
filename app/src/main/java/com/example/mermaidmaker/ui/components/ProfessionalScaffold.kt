package com.example.mermaidmaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.ComponentHeight
import com.example.mermaidmaker.ui.theme.CornerRadius
import com.example.mermaidmaker.ui.theme.Elevation
import com.example.mermaidmaker.ui.theme.IconSize
import com.example.mermaidmaker.ui.theme.Spacing

/**
 * Professional scaffold components with modern design
 */

@Composable
fun ProfessionalScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    colors: androidx.compose.material3.TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.containerColor,
        shadowElevation = Elevation.sm
    ) {
        Column {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.titleContentColor
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.titleContentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (navigationIcon != null && onNavigationClick != null) {
                        ProfessionalIconButton(
                            onClick = onNavigationClick,
                            icon = navigationIcon,
                            variant = ButtonVariant.Ghost,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = { actions() },
                colors = colors.copy(containerColor = Color.Transparent)
            )
        }
    }
}

 

@Composable
fun ProfessionalBottomAppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = Elevation.md
    ) {
        Row(
            modifier = Modifier
                .height(ComponentHeight.bottomBar)
                .padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
            Spacer(modifier = Modifier.weight(1f))
            floatingActionButton()
        }
    }
}

@Composable
fun ProfessionalBottomNavigation(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun ProfessionalSnackbarHost(
    hostState: androidx.compose.material3.SnackbarHostState,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Surface(
                modifier = Modifier
                    .padding(Spacing.lg)
                    .clip(RoundedCornerShape(CornerRadius.md)),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shadowElevation = Elevation.md
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    snackbarData.visuals.actionLabel?.let { actionLabel ->
                        Spacer(modifier = Modifier.width(Spacing.md))
                        ProfessionalButton(
                            onClick = { snackbarData.performAction() },
                            variant = ButtonVariant.Text,
                            size = ButtonSize.Small
                        ) {
                            Text(actionLabel)
                        }
                    }
                }
            }
        }
    )
}