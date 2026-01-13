package com.example.parkingsystem.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.parkingsystem.R
import kotlinx.coroutines.delay

/**
 * Splash screen composable with smooth animations
 *
 * Features:
 * - Logo scale animation (zoom in)
 * - Logo fade in animation
 * - Gradient background
 * - Auto-navigation after 2.5 seconds
 *
 * @param onSplashFinished Callback when splash screen animation is complete
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation state
    var startAnimation by remember { mutableStateOf(false) }

    // Scale animation for logo (zoom in effect)
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    // Alpha animation for fade in
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "alpha_animation"
    )

    // Rotation animation (subtle rotation effect)
    val rotation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -10f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation_animation"
    )

    // Start animation and navigate after delay
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Show splash for 2.5 seconds
        onSplashFinished()
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFFFFFFF)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Logo with animations
        Image(
            painter = painterResource(id = R.drawable.sps),
            contentDescription = "Smart Parking System Logo",
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
                .alpha(alpha)
        )
    }
}
