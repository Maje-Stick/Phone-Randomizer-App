package com.majestick.randomizer

import android.content.Context
import androidx.compose.material3.ColorScheme
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

/**
 * A named palette the user can pick in Settings.
 * [swatch] is the dot shown next to the name in the picker.
 */
data class AppTheme(
    val id: String,
    val label: String,
    val scheme: ColorScheme,
    val swatch: Color
)

private fun dark(
    bg: Long,
    raised: Long,
    accent: Long,
    onAccent: Long,
    text: Long,
    muted: Long,
    line: Long,
    second: Long
): ColorScheme = darkColorScheme(
    primary = Color(accent),
    onPrimary = Color(onAccent),
    primaryContainer = Color(raised),
    onPrimaryContainer = Color(accent),
    secondary = Color(second),
    onSecondary = Color(onAccent),
    background = Color(bg),
    onBackground = Color(text),
    surface = Color(bg),
    onSurface = Color(text),
    surfaceVariant = Color(raised),
    onSurfaceVariant = Color(muted),
    outline = Color(line)
)

private fun light(
    bg: Long,
    raised: Long,
    accent: Long,
    text: Long,
    muted: Long,
    line: Long,
    second: Long
): ColorScheme = lightColorScheme(
    primary = Color(accent),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(raised),
    onPrimaryContainer = Color(accent),
    secondary = Color(second),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(bg),
    onBackground = Color(text),
    surface = Color(bg),
    onSurface = Color(text),
    surfaceVariant = Color(raised),
    onSurfaceVariant = Color(muted),
    outline = Color(line)
)

object Themes {

    val all: List<AppTheme> = listOf(
        AppTheme(
            "midnight", "Midnight",
            dark(0xFF12141C, 0xFF1C1F2B, 0xFFF0A202, 0xFF12141C, 0xFFEDEAE3, 0xFFA8A69E, 0xFF3A3E4C, 0xFF6FD08C),
            Color(0xFFF0A202)
        ),
        AppTheme(
            "forest", "Forest",
            dark(0xFF0F1710, 0xFF18231A, 0xFF7BD389, 0xFF0F1710, 0xFFE6EDE4, 0xFF9AA79A, 0xFF2E3F31, 0xFFD8B26A),
            Color(0xFF7BD389)
        ),
        AppTheme(
            "crimson", "Crimson",
            dark(0xFF17100F, 0xFF241717, 0xFFE5484D, 0xFFFFF5F5, 0xFFF0E6E4, 0xFFAD9C99, 0xFF442C2C, 0xFFE0B15C),
            Color(0xFFE5484D)
        ),
        AppTheme(
            "ocean", "Ocean",
            dark(0xFF0C1620, 0xFF14202D, 0xFF4CC9F0, 0xFF07121A, 0xFFE3ECF2, 0xFF93A6B4, 0xFF27394A, 0xFFF4A261),
            Color(0xFF4CC9F0)
        ),
        AppTheme(
            "violet", "Violet",
            dark(0xFF14101E, 0xFF1F1930, 0xFFB79CED, 0xFF14101E, 0xFFEBE6F2, 0xFFA79FB5, 0xFF383052, 0xFF7BD389),
            Color(0xFFB79CED)
        ),
        AppTheme(
            "mono", "Mono",
            dark(0xFF101010, 0xFF1B1B1B, 0xFFE8E8E8, 0xFF101010, 0xFFF2F2F2, 0xFF9A9A9A, 0xFF343434, 0xFFBDBDBD),
            Color(0xFFE8E8E8)
        ),
        AppTheme(
            "paper", "Paper",
            light(0xFFFBFAF7, 0xFFEDEAE3, 0xFF8A5A00, 0xFF12141C, 0xFF54565E, 0xFFC7C3B9, 0xFF2E6B41),
            Color(0xFF8A5A00)
        ),
        AppTheme(
            "daylight", "Daylight",
            light(0xFFF6F9FC, 0xFFE4ECF4, 0xFF0B6E99, 0xFF10202B, 0xFF4C606E, 0xFFBACBD8, 0xFFC2410C),
            Color(0xFF0B6E99)
        )
    )

    const val DEFAULT_ID = "midnight"

    fun byId(id: String?): AppTheme = all.firstOrNull { it.id == id } ?: all.first()
}

object ThemeStore {
    private const val PREFS = "randomizer_settings"
    private const val KEY = "theme"

    fun load(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, Themes.DEFAULT_ID) ?: Themes.DEFAULT_ID

    fun save(context: Context, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, id).apply()
    }
}

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
fun RandomizerTheme(themeId: String, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Themes.byId(themeId).scheme,
        typography = AppTypography,
        content = content
    )
}
