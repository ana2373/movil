package com.example.kaffacafeteria.ui.theme

import androidx.compose.ui.graphics.Color

// === Paleta (café, verde, negro y blanco) ===
val CafeOscuro = Color(0xFF3E2723)
val CafeTabaco = Color(0xFF4E342E)
val CafeClaro = Color(0xFFC9A68D)
val Negro = Color(0xFF111111)
val Blanco = Color(0xFFFFFFFF)

// Verde principal
val PrimaryGreen = Color(0xFF14532D)
val PrimaryGreenDark = Color(0xFF82B58B)
val SecondaryGreen = Color(0xFF2D6A4F)
val SecondaryGreenDark = Color(0xFF9BC4A2)
val VerdeClaro = Color(0xFF43A047)
val LightGreen = Color(0xFFD8F3DC)
val LightGreenDark = Color(0xFF1E3B26)

// Cafe (accesorios y precios)
val Terracotta = Color(0xFF4E342E)
val TerracottaDark = Color(0xFFC9A68D)
val LightTerracotta = Color(0xFFEDE0D6)
val LightTerracottaDark = Color(0xFF3A2B21)

val BackgroundCream = Color(0xFFFFFFFF)
val CardWhite = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1C1A18)
val BackgroundDark = Color(0xFF121212)

val CoffeeBrown = Color(0xFF6F4E37)
val CoffeeBrownDark = Color(0xFFC9A98F)
val LightBrown = Color(0xFFEAD7C0)
val LightBrownDark = Color(0xFF3A2E24)

val TextGray = Color(0xFF6B6259)
val TextGrayDark = Color(0xFFB8AFA4)

val PriceColor = Color(0xFF4E342E)
val PriceColorDark = Color(0xFFC9A68D)

val NearBlack = Color(0xFF111111)
val NearBlackDark = Color(0xFFF0EBE4)

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
    // Café / espresso
    PromoPalette(
        backgroundStart = Color(0xFF1B100C),
        backgroundEnd = Color(0xFF3E2723),
        accent = Color(0xFFC9A68D),
        accentDark = Color(0xFF6F4E37),
        cardBackground = Color(0xFFFFF8F0),
        titleText = Color(0xFF3E2723),
        subtitleText = Color(0xFF6F4E37),
        highlightText = Color(0xFF4E342E)
    ),
    // Verde / menta
    PromoPalette(
        backgroundStart = Color(0xFF0E2F24),
        backgroundEnd = Color(0xFF14532D),
        accent = Color(0xFF82B58B),
        accentDark = Color(0xFF2E7D32),
        cardBackground = Color(0xFFF2FBF2),
        titleText = Color(0xFF14532D),
        subtitleText = Color(0xFF2D6A4F),
        highlightText = Color(0xFF2E7D32)
    ),
    // Negro / café oscuro
    PromoPalette(
        backgroundStart = Color(0xFF111111),
        backgroundEnd = Color(0xFF2B231D),
        accent = Color(0xFFD7CFC5),
        accentDark = Color(0xFF4E342E),
        cardBackground = Color(0xFFF5F2EE),
        titleText = Color(0xFF1A1713),
        subtitleText = Color(0xFF4E342E),
        highlightText = Color(0xFF3E2723)
    ),
    // Café latte
    PromoPalette(
        backgroundStart = Color(0xFF3A2B21),
        backgroundEnd = Color(0xFF6F4E37),
        accent = Color(0xFFE8CDB5),
        accentDark = Color(0xFF9A6A4A),
        cardBackground = Color(0xFFFDF7EF),
        titleText = Color(0xFF4B2E1E),
        subtitleText = Color(0xFF6F4E37),
        highlightText = Color(0xFF8B5A3B)
    ),
    // Verde oliva
    PromoPalette(
        backgroundStart = Color(0xFF1C2B1A),
        backgroundEnd = Color(0xFF2D4A2A),
        accent = Color(0xFF9CCB98),
        accentDark = Color(0xFF2E7D32),
        cardBackground = Color(0xFFF4FAF0),
        titleText = Color(0xFF14532D),
        subtitleText = Color(0xFF2D6A4F),
        highlightText = Color(0xFF2E7D32)
    ),
    // Café tostado
    PromoPalette(
        backgroundStart = Color(0xFF241108),
        backgroundEnd = Color(0xFF5B2C16),
        accent = Color(0xFFE0A06A),
        accentDark = Color(0xFF9A4A1B),
        cardBackground = Color(0xFFFFF6ED),
        titleText = Color(0xFF4B220F),
        subtitleText = Color(0xFF6F4E37),
        highlightText = Color(0xFF9A4A1B)
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
    // Teal / verde azulado
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