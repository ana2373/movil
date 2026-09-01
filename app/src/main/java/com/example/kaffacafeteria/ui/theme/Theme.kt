package com.example.kaffacafeteria.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Color_OutlineLight = Color(0xFFCFC6B8)
private val Color_OutlineVariantLight = Color(0xFFE7DFD2)
private val Color_OutlineDark = Color(0xFF5A544A)
private val Color_OutlineVariantDark = Color(0xFF2A2C27)

private val KaffaLightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = LightGreen,
    onPrimaryContainer = PrimaryGreen,

    secondary = Terracotta,
    onSecondary = White,
    secondaryContainer = LightTerracotta,
    onSecondaryContainer = Terracotta,

    tertiary = CoffeeBrown,
    onTertiary = White,
    tertiaryContainer = LightBrown,
    onTertiaryContainer = CoffeeBrown,

    background = BackgroundCream,
    onBackground = NearBlack,

    surface = CardWhite,
    onSurface = NearBlack,
    surfaceVariant = BackgroundCream,
    onSurfaceVariant = TextGray,

    outline = Color_OutlineLight,
    outlineVariant = Color_OutlineVariantLight,

    error = ErrorRed,
    onError = White
)

private val KaffaDarkColors = darkColorScheme(
    primary = PrimaryGreenDark,
<<<<<<< HEAD
    onPrimary = NearBlackDark,
=======
    onPrimary = PrimaryGreen,
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    primaryContainer = LightGreenDark,
    onPrimaryContainer = LightGreen,

    secondary = TerracottaDark,
<<<<<<< HEAD
    onSecondary = NearBlackDark,
=======
    onSecondary = NearBlack,
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    secondaryContainer = LightTerracottaDark,
    onSecondaryContainer = LightTerracotta,

    tertiary = CoffeeBrownDark,
<<<<<<< HEAD
    onTertiary = NearBlackDark,
=======
    onTertiary = NearBlack,
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
    tertiaryContainer = LightBrownDark,
    onTertiaryContainer = LightBrown,

    background = BackgroundDark,
    onBackground = NearBlackDark,

    surface = SurfaceDark,
    onSurface = NearBlackDark,
    surfaceVariant = BackgroundDark,
    onSurfaceVariant = TextGrayDark,

    outline = Color_OutlineDark,
    outlineVariant = Color_OutlineVariantDark,

    error = ErrorRedDark,
    onError = NearBlack
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
