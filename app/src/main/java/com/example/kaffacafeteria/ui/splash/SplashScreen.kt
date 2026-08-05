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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaffacafeteria.R
import com.example.kaffacafeteria.ui.theme.*
import com.example.kaffacafeteria.util.createViewModel

@Composable
fun SplashScreen(
    onContinue: () -> Unit,
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
        PromoSplash(onContinue = onContinue)
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
private fun PromoSplash(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2B211A),
                        Color(0xFF4A3826)
                    )
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
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Terracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Coffee, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Kaffa", color = White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("CAFETERÍA", color = LightBrown, fontSize = 10.sp, letterSpacing = 2.sp)
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

            Image(
                painter = painterResource(R.drawable.coffee1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "El sabor que despierta tus mañanas",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Café artesanal preparado con granos seleccionados",
                style = MaterialTheme.typography.bodyLarge,
                color = LightBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("2x1 en Café", fontSize = 30.sp, fontWeight = FontWeight.Black, color = NearBlack)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Antes de las 11:00 am", style = MaterialTheme.typography.titleMedium, color = CoffeeBrown)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Visítanos hoy", style = MaterialTheme.typography.titleMedium, color = Terracotta, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = White)
            ) {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SocialCircle(Icons.Default.Camera)
                SocialCircle(Icons.Default.ThumbUp)
                SocialCircle(Icons.Default.Email)
                SocialCircle(Icons.Default.Chat)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SocialCircle(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = CircleShape, color = White.copy(alpha = 0.15f)) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}
