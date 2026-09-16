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
import coil.compose.AsyncImage
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.ui.admin.Promocion
import com.example.kaffacafeteria.ui.admin.PromocionesViewModel
import com.example.kaffacafeteria.ui.admin.estaExpirada
import com.example.kaffacafeteria.ui.theme.PrimaryGreen
import com.example.kaffacafeteria.ui.theme.PromoPalette
import com.example.kaffacafeteria.ui.theme.PromoPalettes
import com.example.kaffacafeteria.ui.theme.White
import com.example.kaffacafeteria.util.createViewModel

@Composable
fun SplashScreen(
    onContinue: (User?) -> Unit,
    onSessionState: (Boolean) -> Unit,
    viewModel: SplashViewModel = createViewModel { SplashViewModel(it) },
    promosViewModel: PromocionesViewModel = createViewModel { PromocionesViewModel(it) }
) {
    val state = viewModel.uiState
    val promos = promosViewModel.uiState.promociones.filter { it.activa && !it.estaExpirada() }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            onSessionState(state.isLoggedIn)
        }
    }

    if (state.isLoading) {
        LoadingSplash()
    } else {
        // Solo se muestran las promociones creadas por el administrador
        val promo = promos.firstOrNull()
        if (promo == null) {
            BrandingSplash(onContinue = { onContinue(state.user) })
        } else {
            PromoSplash(promo = promo, onContinue = { onContinue(state.user) })
        }
    }
}

@Composable
private fun LoadingSplash() {
    Box(
        modifier = Modifier.fillMaxSize().background(PrimaryGreen),
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
private fun BrandingSplash(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryGreen, Color(0xFF0E2F24))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier.size(86.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Coffee, contentDescription = null, tint = White, modifier = Modifier.size(46.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kaffa", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 44.sp, color = White)
            Text("CAFETERÍA · SENA", color = White.copy(alpha = 0.85f), fontSize = 12.sp, letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen)
            ) {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PromoSplash(promo: Promocion, onContinue: () -> Unit) {
    val palette = PromoPalettes.getOrElse(promo.colorIndex) { PromoPalettes.first() }

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
                IconButton(onClick = onContinue) {
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
                    promo.tipo,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = palette.backgroundStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (promo.imagenUri.isNullOrBlank()) {
                Image(
                    painter = painterResource(R.drawable.coffee1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
            } else {
                AsyncImage(
                    model = promo.imagenUri,
                    contentDescription = promo.nombre,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.coffee1),
                    fallback = painterResource(R.drawable.coffee1),
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                promo.nombre,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                promo.descripcion.ifBlank { "Aprovecha esta oferta de nuestra cafetería" },
                style = MaterialTheme.typography.bodyLarge,
                color = White.copy(alpha = 0.9f),
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
                    Text(promo.nombre, fontSize = 24.sp, fontWeight = FontWeight.Black, color = palette.titleText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(promo.tipo, style = MaterialTheme.typography.titleMedium, color = palette.highlightText, fontWeight = FontWeight.Bold)
                    if (promo.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(promo.descripcion, style = MaterialTheme.typography.bodyMedium, color = palette.subtitleText, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
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

@Composable
private fun SocialCircle(icon: ImageVector, palette: PromoPalette) {
    Surface(shape = CircleShape, color = White.copy(alpha = 0.15f)) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}