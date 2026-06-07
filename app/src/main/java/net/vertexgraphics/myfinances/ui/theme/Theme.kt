package net.vertexgraphics.myfinances.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FocusedControlColor = Color(0xFF48B1EA)
val AppTextColor = Color(0xFF333333)

val LightColors = lightColorScheme(
    primary = Color(0xFF85BAF2),
    primaryContainer = Color(0xFF85BAF2),
    secondary = Color(0xFF9DC6F2),
    secondaryContainer = Color(0xFF9DC6F2),
    tertiary = Color(0xFF48B1EA),
    background = Color(0xFFDAE5F2),
    surface = Color(0xFFDAE5F2),
    surfaceVariant = Color(0xFFC0D0E0),
    surfaceBright = Color(0xFFB7E0FF),
    outlineVariant = Color(0xFF36454F),
    onPrimary = AppTextColor,
    onPrimaryContainer = AppTextColor,
    onSecondary = AppTextColor,
    onSecondaryContainer = AppTextColor,
    onTertiary = AppTextColor,
    onTertiaryContainer = AppTextColor,
    onBackground = AppTextColor,
    onSurface = AppTextColor,
    onSurfaceVariant = AppTextColor
)

@Composable
fun MyFinancesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
