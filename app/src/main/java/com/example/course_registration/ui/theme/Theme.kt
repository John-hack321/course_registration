package com.example.course_registration.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// UoN Brand Color Scheme (Material3) — drives default component theming.
private val UoNColorScheme = lightColorScheme(
    primary          = Color(0xFF003580),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F0FB),
    onPrimaryContainer = Color(0xFF001A40),

    secondary        = Color(0xFF0057B8),
    onSecondary      = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8F0FB),
    onSecondaryContainer = Color(0xFF002A66),

    tertiary         = Color(0xFFF5A623),
    onTertiary       = Color(0xFF1F1300),

    background       = Color(0xFFF4F7FC),
    onBackground     = Color(0xFF0A1628),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF0A1628),
    surfaceVariant   = Color(0xFFEFF3FA),
    onSurfaceVariant = Color(0xFF5A6A85),

    outline          = Color(0xFFE2E8F4),
    outlineVariant   = Color(0xFFEEF2F8),

    error            = Color(0xFFD32F2F),
    onError          = Color(0xFFFFFFFF),
)

@Composable
fun Course_registrationTheme(
    content: @Composable () -> Unit
) {
    // App is brand-locked to the UoN palette — no dynamic/dark theming.
    MaterialTheme(
        colorScheme = UoNColorScheme,
        typography  = Typography,
        content     = content
    )
}
