package com.example.parkingsystem.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkingsystem.model.ParkingSpot
import com.example.parkingsystem.ui.theme.SpotAvailable
import com.example.parkingsystem.ui.theme.SpotBorder
import com.example.parkingsystem.ui.theme.SpotOccupied


/**
 * Composable function to display a single parking spot card
 *
 * @param spot The parking spot data
 * @param onClick Callback when the card is clicked (for testing/simulation)
 * @param modifier Modifier for customization
 */
@Composable
fun ParkingSpotCard(
    spot: ParkingSpot,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animate color transition
    val backgroundColor by animateColorAsState(
        targetValue = if (spot.isOccupied) SpotOccupied else SpotAvailable,
        animationSpec = tween(durationMillis = 300),
        label = "background_color"
    )

    // Animate scale when status changes
    val scale by animateFloatAsState(
        targetValue = if (spot.isOccupied) 0.95f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(2.dp, SpotBorder),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (spot.isOccupied) 2.dp else 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Car icon
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "Car Icon",
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Spot number
            Text(
                text = "Spot ${spot.id}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status text
            Text(
                text = spot.getStatusText(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}