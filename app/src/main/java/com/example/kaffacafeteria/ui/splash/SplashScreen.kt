package com.example.kaffacafeteria.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.domain.model.User
import com.example.kaffacafeteria.ui.theme.PrimaryGreen
import com.example.kaffacafeteria.ui.theme.White
import com.example.kaffacafeteria.util.createViewModel
import kotlinx.coroutines.delay

/** Tiempo que se muestra el splash antes de continuar solo. */
private const val splashDuracionMs = 2000L

/**
 * Splash de arranque: logo de Kaffa.
 *
 * Las promociones ya no se muestran aquí; aparecen como pantallas
 * emergentes en el Inicio y en el Carrito.
 */
@Composable
fun SplashScreen(
    onContinue: (User?) -> Unit,
    onSessionState: (Boolean) -> Unit,
    viewModel: SplashViewModel = createViewModel { SplashViewModel(it) }
) {
    val state = viewModel.uiState
    // Evita navegar dos veces si el usuario pulsa "Entrar" mientras el
    // avance automático sigue en curso.
    var yaContinuado by remember { mutableStateOf(false) }

    fun continuar() {
        if (!yaContinuado) {
            yaContinuado = true
            onContinue(state.user)
        }
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            onSessionState(state.isLoggedIn)
        }
    }

    // El splash se retira solo cuando la sesión ya se consultó.
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(splashDuracionMs)
            continuar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryGreen, Color(0xFF0E2F24)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    // El icono adaptativo `R.mipmap.kaffa_logo_3` no se puede
                    // pintar con `painterResource` (es un XML y Compose sólo
                    // admite vectores o imágenes rasterizadas); se usa su
                    // foreground, que es la misma imagen del icono en WebP.
                    painter = painterResource(R.mipmap.kaffa_logo_3_foreground),
                    contentDescription = "Kaffa",
                    modifier = Modifier.size(112.dp).clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "Kaffa",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                color = White
            )
            Text(
                "CAFETERÍA · SENA",
                color = White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(color = White, strokeWidth = 2.dp)

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { continuar() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}
