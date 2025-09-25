package com.example.mermaidmaker.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberOrientationAwareMaxHeight(portraitFraction: Float = 0.5f, landscapeFraction: Float = 0.7f): Dp {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fraction = if (isLandscape) landscapeFraction else portraitFraction
    return configuration.screenHeightDp.dp * fraction
}


@Composable
fun rememberOrientationAwareMaxWidth(portraitFraction: Float = 0.9f, landscapeFraction: Float = 0.7f): Dp {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fraction = if (isLandscape) landscapeFraction else portraitFraction
    return configuration.screenWidthDp.dp * fraction
}

@Composable
fun rememberCappedDialogWidth(
    baseWidth: Dp,
    portraitFraction: Float = 0.9f,
    landscapeFraction: Float = 0.6f
): Dp {
    val maxAllowed = rememberOrientationAwareMaxWidth(
        portraitFraction = portraitFraction,
        landscapeFraction = landscapeFraction
    )
    return if (baseWidth < maxAllowed) baseWidth else maxAllowed
}

@Composable
fun rememberOrientationAwarePadding(
    horizontalPortrait: Dp,
    horizontalLandscape: Dp,
    verticalPortrait: Dp,
    verticalLandscape: Dp
): PaddingValues {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val horizontal = if (isLandscape) horizontalLandscape else horizontalPortrait
    val vertical = if (isLandscape) verticalLandscape else verticalPortrait
    return PaddingValues(horizontal = horizontal, vertical = vertical)
}


