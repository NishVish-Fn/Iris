package com.parsec.molfunnel.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = CardBg,
    primaryContainer = InkPrimary,
    onPrimaryContainer = CardBg,
    background = CanvasBg,
    onBackground = InkPrimary,
    surface = CardBg,
    onSurface = InkPrimary,
    surfaceVariant = CanvasBg,
    onSurfaceVariant = InkSecondary,
    outline = HairlineBorder
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp), // 24dp for Cards per Section 8
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MolFunnelTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
