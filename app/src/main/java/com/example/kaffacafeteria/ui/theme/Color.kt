package com.example.kaffacafeteria.ui.theme

import androidx.compose.ui.graphics.Color

// === Paleta principal (cafetería: verde oliva, beige, terracota y café) ===
val PrimaryGreen = Color(0xFF14532D)
<<<<<<< HEAD
val PrimaryGreenDark = Color(0xFF5E8C63)
val SecondaryGreen = Color(0xFF2D6A4F)
val SecondaryGreenDark = Color(0xFF5E9B76)
val LightGreen = Color(0xFFD8F3DC)
val LightGreenDark = Color(0xFF334A38)

val Terracotta = Color(0xFFC85A2E)
val TerracottaDark = Color(0xFFE0A084)
val LightTerracotta = Color(0xFFFBE4D5)
val LightTerracottaDark = Color(0xFF55342A)

val BackgroundCream = Color(0xFFF5F0E6)
val CardWhite = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF212225)
val BackgroundDark = Color(0xFF191A1C)

val CoffeeBrown = Color(0xFF6F4E37)
val CoffeeBrownDark = Color(0xFFC9A98F)
val LightBrown = Color(0xFFEAD7C0)
val LightBrownDark = Color(0xFF3A2E24)

val TextGray = Color(0xFF6B7280)
val TextGrayDark = Color(0xFFB8B2A8)

val PriceColor = Color(0xFFC85A2E)
val PriceColorDark = Color(0xFFE0A084)

val NearBlack = Color(0xFF1A1713)
val NearBlackDark = Color(0xFFE6E1D9)

val ErrorRed = Color(0xFFB3261E)
val ErrorRedDark = Color(0xFFE59890)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

data class PromoPalette(
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val accent: Color,
    val accentDark: Color,
    val cardBackground: Color,
    val titleText: Color,
    val subtitleText: Color,
    val highlightText: Color
)

val PromoPalettes = listOf(
    // Terracota / naranja cálido
    PromoPalette(
        backgroundStart = Color(0xFF2B211A),
        backgroundEnd = Color(0xFF6B3A2A),
        accent = Color(0xFFE07A3F),
        accentDark = Color(0xFFC85A2E),
        cardBackground = Color(0xFFFFF8F2),
        titleText = Color(0xFF2B211A),
        subtitleText = Color(0xFF6F4E37),
        highlightText = Color(0xFFC85A2E)
    ),
    // Verde esmeralda / menta
    PromoPalette(
        backgroundStart = Color(0xFF0E2F24),
        backgroundEnd = Color(0xFF14532D),
        accent = Color(0xFF66BB6A),
        accentDark = Color(0xFF2E7D32),
        cardBackground = Color(0xFFF2FBF2),
        titleText = Color(0xFF14532D),
        subtitleText = Color(0xFF2D6A4F),
        highlightText = Color(0xFF2E7D32)
    ),
    // Azul índigo / azul cielo
    PromoPalette(
        backgroundStart = Color(0xFF16264B),
        backgroundEnd = Color(0xFF1E3A8A),
        accent = Color(0xFF60A5FA),
        accentDark = Color(0xFF2563EB),
        cardBackground = Color(0xFFF1F6FF),
        titleText = Color(0xFF1E3A8A),
        subtitleText = Color(0xFF3B5B9E),
        highlightText = Color(0xFF2563EB)
    ),
    // Morado / lavanda
    PromoPalette(
        backgroundStart = Color(0xFF2D1B3A),
        backgroundEnd = Color(0xFF4C1D95),
        accent = Color(0xFFA78BFA),
        accentDark = Color(0xFF7C3AED),
        cardBackground = Color(0xFFF8F4FF),
        titleText = Color(0xFF4C1D95),
        subtitleText = Color(0xFF7C5BB8),
        highlightText = Color(0xFF7C3AED)
    ),
    // Rosado / frambuesa
    PromoPalette(
        backgroundStart = Color(0xFF3A1230),
        backgroundEnd = Color(0xFF831843),
        accent = Color(0xFFF472B6),
        accentDark = Color(0xFFDB2777),
        cardBackground = Color(0xFFFFF3F8),
        titleText = Color(0xFF831843),
        subtitleText = Color(0xFF9D4B6E),
        highlightText = Color(0xFFDB2777)
    ),
    // Rojo coral / tomate
    PromoPalette(
        backgroundStart = Color(0xFF3A1111),
        backgroundEnd = Color(0xFF7F1D1D),
        accent = Color(0xFFF87171),
        accentDark = Color(0xFFDC2626),
        cardBackground = Color(0xFFFFF5F5),
        titleText = Color(0xFF7F1D1D),
        subtitleText = Color(0xFF9D4B4B),
        highlightText = Color(0xFFDC2626)
    ),
    // Ámbar / dorado
    PromoPalette(
        backgroundStart = Color(0xFF33240C),
        backgroundEnd = Color(0xFF78350F),
        accent = Color(0xFFFBBF24),
        accentDark = Color(0xFFD97706),
        cardBackground = Color(0xFFFFFBF0),
        titleText = Color(0xFF78350F),
        subtitleText = Color(0xFF9A6A2E),
        highlightText = Color(0xFFD97706)
    ),
    // Teal / cian profundo
    PromoPalette(
        backgroundStart = Color(0xFF0B2B2B),
        backgroundEnd = Color(0xFF134E4A),
        accent = Color(0xFF2DD4BF),
        accentDark = Color(0xFF0D9488),
        cardBackground = Color(0xFFF0FDFA),
        titleText = Color(0xFF134E4A),
        subtitleText = Color(0xFF2D7A75),
        highlightText = Color(0xFF0D9488)
    )
)
=======
val PrimaryGreenDark = Color(0xFF7CC98A)
val SecondaryGreen = Color(0xFF2D6A4F)
val SecondaryGreenDark = Color(0xFF52B788)
val LightGreen = Color(0xFFD8F3DC)
val LightGreenDark = Color(0xFF1B4332)

val Terracotta = Color(0xFFC85A2E)
val TerracottaDark = Color(0xFFF2A173)
val LightTerracotta = Color(0xFFFBE4D5)
val LightTerracottaDark = Color(0xFF5A2A13)

val BackgroundCream = Color(0xFFF5F0E6)
val CardWhite = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1B1E1A)
val BackgroundDark = Color(0xFF121410)

val CoffeeBrown = Color(0xFF6F4E37)
val CoffeeBrownDark = Color(0xFFD0A98F)
val LightBrown = Color(0xFFEAD7C0)
val LightBrownDark = Color(0xFF3E2E23)

val TextGray = Color(0xFF6B7280)
val TextGrayDark = Color(0xFFC7C3BA)

val PriceColor = Color(0xFFC85A2E)
val PriceColorDark = Color(0xFFF2A173)

val NearBlack = Color(0xFF1A1713)
val NearBlackDark = Color(0xFFEDEAE4)

val ErrorRed = Color(0xFFB3261E)
val ErrorRedDark = Color(0xFFF2B8B5)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
>>>>>>> 7f72da0ee7bae7622924dee7366abac3eab17855
