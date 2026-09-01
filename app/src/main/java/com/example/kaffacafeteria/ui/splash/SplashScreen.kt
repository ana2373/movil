package com.example.kaffacafeteria.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.ui.theme.PromoPalette
import com.example.kaffacafeteria.ui.theme.PromoPalettes
import com.example.kaffacafeteria.ui.theme.White
import com.example.kaffacafeteria.util.createViewModel
import kotlin.random.Random

data class PromoData(
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val etiqueta: String
)

val Promos = listOf(
    PromoData("2x1 en Café", "Antes de las 11:00 am", "Visítanos hoy", "OFERTA"),
    PromoData("Café del Mes", "Latte de Caramelo", "Prueba nuestra nueva especialidad", "NOVEDAD"),
    PromoData("Happy Hour", "Tarde de postres", "2x1 en postres de 3 a 6 pm", "PROMO"),
    PromoData("Desayuno Completo", "Pan + café", "El impulso que necesitas para tu día", "COMBO"),
    PromoData("Noche de Café", "Bebidas calientes 2x1", "Después de las 6 pm", "NOCHE"),
    PromoData("Repostería del Día", "Pan y postres frescos", "Elaborados en la cafetería del SENA", "FRESCO"),
    PromoData("Café de Especialidad", "Granos seleccionados", "Preparado con los mejores granos", "GRANO"),
    PromoData("Combo Pan + Postre", "Acompañamiento perfecto", "El dúo ideal para tu pausa", "COMBO")
)

@Composable
fun SplashScreen(
    onContinue: (User?) -> Unit,
    onSessionState: (Boolean) -> Unit,
    viewModel: SplashViewModel = createViewModel { SplashViewModel(it) }
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            onSessionState(state.isLoggedIn)
        }
    }

    if (state.isLoading) {
        LoadingSplash()
    } else {
        PromoSplash(user = state.user, onContinue = onContinue)
    }
}

@Composable
private fun LoadingSplash() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF14532D)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Kaffa", style = MaterialTheme.typography.displayLarge, color = White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = White, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun PromoSplash(onContinue: (User?) -> Unit, user: User?) {
    // Elegir una paleta diferente al azar cada vez que se abre la app
    val promoIndex = remember { Random.nextInt(Promos.size) }
    val paletteIndex = remember { Random.nextInt(PromoPalettes.size) }
    val promo = Promos[promoIndex]
    val palette = PromoPalettes[paletteIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(palette.backgroundStart, palette.backgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(palette.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Coffee, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Kaffa", color = White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("CAFETERÍA", color = White.copy(alpha = 0.8f), fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                }
                IconButton(onClick = { onContinue(user) }) {
                    Surface(shape = CircleShape, color = White.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.accent,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    promo.etiqueta,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = palette.backgroundStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.coffee1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                promo.titulo,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                promo.subtitulo,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = palette.accentBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                promo.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(promo.titulo, fontSize = 30.sp, fontWeight = FontWeight.Black, color = palette.titleText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(promo.subtitulo, style = MaterialTheme.typography.titleMedium, color = palette.subtitleText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(promo.descripcion, style = MaterialTheme.typography.titleMedium, color = palette.highlightText, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onContinue(user) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.backgroundStart)
            ) {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SocialCircle(Icons.Default.Camera, palette)
                SocialCircle(Icons.Default.ThumbUp, palette)
                SocialCircle(Icons.Default.Email, palette)
                SocialCircle(Icons.Default.Chat, palette)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private val PromoPalette.accentBright: Color
    get() = Color(
        red = (accent.red * 255 + 80) / 255f,
        green = (accent.green * 255 + 80) / 255f,
        blue = (accent.blue * 255 + 80) / 255f
    ).let { c ->
        Color(
            red = c.red.coerceAtMost(1f),
            green = c.green.coerceAtMost(1f),
            blue = c.blue.coerceAtMost(1f)
        )
    }

@Composable
private fun SocialCircle(icon: ImageVector, palette: PromoPalette) {
    Surface(shape = CircleShape, color = White.copy(alpha = 0.15f)) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}
