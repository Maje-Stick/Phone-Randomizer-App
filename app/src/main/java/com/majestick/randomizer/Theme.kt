package com.majestick.randomizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF12141C)
private val InkRaised = Color(0xFF1C1F2B)
private val Amber = Color(0xFFF0A202)
private val Mint = Color(0xFF6FD08C)
private val Bone = Color(0xFFEDEAE3)

private val DarkScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Ink,
    primaryContainer = Color(0xFF3A2C06),
    onPrimaryContainer = Amber,
    secondary = Mint,
    onSecondary = Ink,
    background = Ink,
    onBackground = Bone,
    surface = Ink,
    onSurface = Bone,
    surfaceVariant = InkRaised,
    onSurfaceVariant = Color(0xFFA8A69E),
    outline = Color(0xFF3A3E4C)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF8A5A00),
    onPrimary = Color.White,
    secondary = Color(0xFF2E6B41),
    background = Color(0xFFFBFAF7),
    onBackground = Ink,
    surface = Color(0xFFFBFAF7),
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDEAE3),
    onSurfaceVariant = Color(0xFF54565E),
    outline = Color(0xFFC7C3B9)
)

/**
 * One family, three jobs: an oversized weight for results, a tight medium for
 * controls, and a relaxed body. The result is the only thing allowed to shout.
 */
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 68.sp,
        lineHeight = 72.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)

@Composable
fun RandomizerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content
    )
}
