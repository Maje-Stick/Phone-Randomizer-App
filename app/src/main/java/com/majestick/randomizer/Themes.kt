package com.majestick.randomizer

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    val swatch: Color,
    /**
     * Vertical gradient painted behind everything. Empty means a flat fill of
     * the scheme background, which is what the plain palettes want. The scenic
     * themes stack three or four stops to suggest depth -- a horizon, a canopy,
     * a skyline glow -- without shipping a single bitmap.
     */
    val backdrop: List<Color> = emptyList(),
    /** Soft radial wash laid over the gradient. A sun, a streetlight, a nebula. */
    val glow: Color? = null,
    /** Artistic themes carry artwork and their own sound set; plain ones do not. */
    val artistic: Boolean = false
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
        ),

        // --- scenic ---

        AppTheme(
            "nature", "Nature",
            dark(0xFF0E1A12, 0xFF17281B, 0xFF9BE07F, 0xFF0B160F, 0xFFE9F1E5, 0xFF9CB09B, 0xFF2C4433, 0xFFE0B15C),
            Color(0xFF9BE07F),
            backdrop = listOf(
                Color(0xFF1B3A24),
                Color(0xFF13291A),
                Color(0xFF0E1A12),
                Color(0xFF0A130D)
            ),
            glow = Color(0x2E9BE07F),
            artistic = true
        ),
        AppTheme(
            "sea", "Sea",
            dark(0xFF061620, 0xFF0F2634, 0xFF56D4E8, 0xFF04121A, 0xFFE0F0F6, 0xFF8FAFBE, 0xFF1E4155, 0xFFF4C25F),
            Color(0xFF56D4E8),
            backdrop = listOf(
                Color(0xFF0E4258),
                Color(0xFF0A2E40),
                Color(0xFF071E2C),
                Color(0xFF04121A)
            ),
            glow = Color(0x3356D4E8),
            artistic = true
        ),
        AppTheme(
            "space", "Space",
            dark(0xFF0A0814, 0xFF171331, 0xFFB79CED, 0xFF0A0814, 0xFFEDE9F7, 0xFFA49CBD, 0xFF322A57, 0xFF6FD08C),
            Color(0xFFB79CED),
            backdrop = listOf(
                Color(0xFF241847),
                Color(0xFF150F2E),
                Color(0xFF0C0919),
                Color(0xFF05040C)
            ),
            glow = Color(0x38A187E8),
            artistic = true
        ),
        AppTheme(
            "citynight", "Night City",
            dark(0xFF0D0A14, 0xFF1D1526, 0xFFFF7BC8, 0xFF120C18, 0xFFF3E9F3, 0xFFB09DB2, 0xFF3A2A45, 0xFF4CC9F0),
            Color(0xFFFF7BC8),
            backdrop = listOf(
                Color(0xFF3A1B45),
                Color(0xFF241432),
                Color(0xFF150E1F),
                Color(0xFF0D0A14)
            ),
            glow = Color(0x33FF7BC8),
            artistic = true
        ),
        AppTheme(
            "autumn", "Autumn",
            dark(0xFF1A1009, 0xFF2A1A10, 0xFFF08A3C, 0xFF1A1009, 0xFFF5E7DA, 0xFFC0A28C, 0xFF4A3020, 0xFFD8C15C),
            Color(0xFFF08A3C),
            backdrop = listOf(
                Color(0xFF5A2A12),
                Color(0xFF3A1C0E),
                Color(0xFF24140B),
                Color(0xFF150C06)
            ),
            glow = Color(0x33F0A93C),
            artistic = true
        ),
        AppTheme(
            "aero", "Frutiger Aero",
            dark(0xFF07202B, 0xFF0E3140, 0xFF5FE3C8, 0xFF042028, 0xFFE8F7FA, 0xFF93BDC6, 0xFF1B4A5C, 0xFFA8E063),
            Color(0xFF5FE3C8),
            backdrop = listOf(
                Color(0xFF1E7FA8),
                Color(0xFF116383),
                Color(0xFF0A3F55),
                Color(0xFF06202B)
            ),
            glow = Color(0x40A8E7F5),
            artistic = true
        ),
        AppTheme(
            "digital", "Digital",
            dark(0xFF060A08, 0xFF0E1A14, 0xFF3DF08A, 0xFF041008, 0xFFDDF5E6, 0xFF89AD9A, 0xFF1C3A2A, 0xFF4CC9F0),
            Color(0xFF3DF08A),
            backdrop = listOf(
                Color(0xFF0A2A1C),
                Color(0xFF071C13),
                Color(0xFF04110B),
                Color(0xFF020705)
            ),
            glow = Color(0x2E3DF08A),
            artistic = true
        )
    )

    const val DEFAULT_ID = "midnight"

    fun byId(id: String?): AppTheme = all.firstOrNull { it.id == id } ?: all.first()

    val plain: List<AppTheme> get() = all.filterNot { it.artistic }
    val artistic: List<AppTheme> get() = all.filter { it.artistic }
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

/**
 * Paints whatever sits behind the whole app: a gradient always, a radial wash
 * when the theme has one, and a piece of artwork when a drawable exists for this
 * theme and screen.
 *
 * Artwork is looked up by name rather than wired in by id, so dropping
 * `art_sea_home.png` into res/drawable is the entire integration step. A missing
 * file simply means no artwork, and each screen falls back to the theme's home
 * image before giving up. Every screen paints its Scaffold transparently so this
 * shows through.
 */
@Composable
fun ThemeBackdrop(themeId: String, screen: String, content: @Composable () -> Unit) {
    val theme = Themes.byId(themeId)
    val context = LocalContext.current

    val artId = remember(themeId, screen) {
        artResource(context, "art_${theme.id}_$screen")
            ?: artResource(context, "art_${theme.id}_home")
    }

    val stops = if (theme.backdrop.size >= 2) theme.backdrop
    else listOf(theme.scheme.background, theme.scheme.background)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(stops))
    ) {
        if (artId != null) {
            Image(
                painter = painterResource(artId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Artwork behind live text needs holding back or nothing is legible.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                theme.scheme.background.copy(alpha = 0.62f),
                                theme.scheme.background.copy(alpha = 0.82f)
                            )
                        )
                    )
            )
        } else {
            theme.glow?.let { tint ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(tint, Color.Transparent),
                                radius = 900f
                            )
                        )
                )
            }
        }
        content()
    }
}

@Suppress("DiscouragedApi")
private fun artResource(context: android.content.Context, name: String): Int? {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (id != 0) id else null
}
