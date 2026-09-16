package com.example.kaffacafeteria.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Color_OutlineLight = Color(0xFFCFC7BC)
private val Color_OutlineVariantLight = Color(0xFFE8E1D6)
private val Color_OutlineDark = Color(0xFF5C554C)
private val Color_OutlineVariantDark = Color(0xFF33302B)

val DarkBackground = Color(0xFF080C09)

private val KaffaLightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = LightGreen,
    onPrimaryContainer = PrimaryGreen,

    secondary = CafeOscuro,
    onSecondary = White,
    secondaryContainer = LightTerracotta,
    onSecondaryContainer = CafeOscuro,

    tertiary = Negro,
    onTertiary = White,
    tertiaryContainer = Color(0xFFE8E4DE),
    onTertiaryContainer = Negro,

    background = BackgroundCream,
    onBackground = NearBlack,

    surface = CardWhite,
    onSurface = NearBlack,
    surfaceVariant = Color(0xFFF4EFE9),
    onSurfaceVariant = TextGray,

    outline = Color_OutlineLight,
    outlineVariant = Color_OutlineVariantLight,

    error = ErrorRed,
    onError = White
)

private val KaffaDarkColors = darkColorScheme(
    primary = Color(0xFF86C99A),
    onPrimary = Color(0xFF00210D),
    primaryContainer = Color(0xFF123B21),
    onPrimaryContainer = Color(0xFFADE4BD),

    secondary = Color(0xFFA9D9B2),
    onSecondary = Color(0xFF04260F),
    secondaryContainer = Color(0xFF16462A),
    onSecondaryContainer = Color(0xFFCBF1D1),

    tertiary = Color.White,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF1A201B),
    onTertiaryContainer = Color.White,

    background = DarkBackground,
    onBackground = Color.White,

    surface = Color(0xFF10150F),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1D251E),
    onSurfaceVariant = Color(0xFFC2CDC3),

    outline = Color(0xFF8A968C),
    outlineVariant = Color(0xFF262F27),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun KaffaTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KaffaDarkColors else KaffaLightColors,
        typography = Typography,
        content = content
    )
}

@Composable
fun isDarkModeActive(): Boolean = MaterialTheme.colorScheme.background == DarkBackground

@Composable
fun ink(base: Color): Color {
    if (!isDarkModeActive()) return base
    return when (base) {
        Negro, NearBlack, Black -> Color(0xFFDDE3DD)
        PrimaryGreen, SecondaryGreen, PrimaryGreenDark, SecondaryGreenDark, VerdeClaro,
        CafeOscuro, CoffeeBrown, Terracotta, PriceColor -> PrimaryGreenDark
        TextGray -> TextGrayDark
        else -> base
    }
}

@Composable
fun fill(base: Color): Color {
    if (!isDarkModeActive()) return base
    return when (base) {
        Negro, NearBlack, Black -> Color(0xFFE6EAE6)
        PrimaryGreen, SecondaryGreen, CafeOscuro, CoffeeBrown, Terracotta, PriceColor -> VerdeClaro
        else -> base
    }
}