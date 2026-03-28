package com.vavarda.clicker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vavarda.clicker.R

private val VavardaDarkColors = darkColorScheme(
    primary = Color(0xFFB78DFF),
    onPrimary = Color(0xFF1D0D3A),
    secondary = Color(0xFFD8B9FF),
    background = Color(0xFF0A0615),
    surface = Color(0xFF1B1030),
    onSurface = Color(0xFFF2E6FF)
)

val VavardaDisplayFont = FontFamily(
    Font(R.font.oranienbaum_regular, FontWeight.Normal)
)

val VavardaBodyFont = FontFamily(
    Font(R.font.alegreya_sans_regular, FontWeight.Normal),
    Font(R.font.alegreya_sans_medium, FontWeight.Medium),
    Font(R.font.alegreya_sans_bold, FontWeight.Bold)
)

val VavardaAccentFont = FontFamily(
    Font(R.font.russo_one_regular, FontWeight.Normal)
)

val VavardaNarrativeFont = FontFamily(
    Font(R.font.marck_script_regular, FontWeight.Normal)
)

private val VavardaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = VavardaDisplayFont,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.6.sp
    ),
    displayMedium = TextStyle(
        fontFamily = VavardaDisplayFont,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = VavardaAccentFont,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.3.sp
    ),
    titleLarge = TextStyle(
        fontFamily = VavardaDisplayFont,
        fontWeight = FontWeight.Normal
    ),
    titleMedium = TextStyle(
        fontFamily = VavardaAccentFont,
        fontWeight = FontWeight.Normal
    ),
    bodyLarge = TextStyle(
        fontFamily = VavardaBodyFont,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = VavardaBodyFont,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontFamily = VavardaNarrativeFont,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = VavardaAccentFont,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontFamily = VavardaBodyFont,
        fontWeight = FontWeight.Medium
    )
)

@Composable
fun VavardaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VavardaDarkColors,
        typography = VavardaTypography,
        content = content
    )
}
