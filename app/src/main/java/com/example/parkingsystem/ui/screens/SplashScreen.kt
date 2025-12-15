package com.example.parkingsystem.ui.screens


import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


/**
 * Splash screen composable that displays the app logo
 *
 * @param onSplashFinished Callback when splash screen animation is complete
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")

    // Scale animation for logo
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    // Alpha animation for fade in
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "alpha"
    )

    // Navigate to main screen after delay
    LaunchedEffect(Unit) {
        delay(6000) // Show splash for 2.5 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo image
            Image(
                painter = painterResource(id = com.example.parkingsystem.R.drawable.sps),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(250.dp)
                    .scale(scale)
                    .alpha(alpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            Text(
                text = "Smart Parking System",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3A52),
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Park Smart, Live Smart",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2196F3),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}