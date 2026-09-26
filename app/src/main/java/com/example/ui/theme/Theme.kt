package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val VoxVeloColorScheme = darkColorScheme(
  primary = AccentRed,
  onPrimary = TextPrimary,
  primaryContainer = AccentRedDim,
  onPrimaryContainer = TextPrimary,
  secondary = AccentTeal,
  onSecondary = DarkBg,
  secondaryContainer = AccentTealDim,
  onSecondaryContainer = AccentTeal,
  tertiary = AccentAmber,
  onTertiary = DarkBg,
  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurface2,
  onSurfaceVariant = TextDim,
  outline = DarkLineStrong,
  outlineVariant = DarkLine
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = VoxVeloColorScheme,
    typography = Typography,
    content = content
  )
}

