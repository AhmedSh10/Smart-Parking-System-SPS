package com.example.parkingsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkingsystem.bluetooth.ConnectionState
import com.example.parkingsystem.model.ParkingState
import com.example.parkingsystem.ui.components.FireAlertDialog
import com.example.parkingsystem.ui.components.ParkingSpotCard
import com.example.parkingsystem.ui.components.StatisticsCard
import com.example.parkingsystem.ui.theme.SpotAvailable
import com.example.parkingsystem.ui.theme.SpotOccupied
import com.example.parkingsystem.viewmodel.ParkingViewModel

/**
 * Main parking screen composable
 * 
 * @param viewModel The parking view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingScreen(
    viewModel: ParkingViewModel = viewModel(),
    onBluetoothClick: () -> Unit = {}
) {
    val parkingState by viewModel.parkingState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart Parking System",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Bluetooth button
                    IconButton(onClick = onBluetoothClick) {
                        Icon(
                            imageVector = if (connectionState.isConnected()) {
                                Icons.Default.BluetoothConnected
                            } else {
                                Icons.Default.Bluetooth
                            },
                            contentDescription = "Bluetooth"
                        )
                    }
                    
                    // Refresh button
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = parkingState) {
                is ParkingState.Loading -> {
                    LoadingContent()
                }
                
                is ParkingState.Success -> {
                    SuccessContent(
                        state = state,
                        viewModel = viewModel
                    )
                    
                    // Show fire alert dialog if active
                    if (state.fireAlertActive) {
                        FireAlertDialog(
                            onDismiss = { viewModel.dismissFireAlert() }
                        )
                    }
                }
                
                is ParkingState.Error -> {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

/**
 * Loading state content
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading...",
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Success state content
 */
@Composable
private fun SuccessContent(
    state: ParkingState.Success,
    viewModel: ParkingViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Connection status banner
        if (connectionState.isConnected()) {
            ConnectionStatusBanner(connectionState = connectionState)
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Statistics Section
        Text(
            text = "Garage Statistics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticsCard(
                label = "Available",
                count = state.getAvailableCount(),
                color = SpotAvailable,
                modifier = Modifier.weight(1f)
            )
            
            StatisticsCard(
                label = "Occupied",
                count = state.getOccupiedCount(),
                color = SpotOccupied,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Parking Spots Section
        Text(
            text = "Parking Spots",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Grid of parking spots
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(580.dp),
            userScrollEnabled = false
        ) {
            items(state.spots) { spot ->
                ParkingSpotCard(
                    spot = spot,
                    onClick = {
                        // Toggle spot status for testing
                        viewModel.updateSpotStatus(spot.id, !spot.isOccupied)
                    },
                    modifier = Modifier.height(180.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control Buttons (for testing)
        Text(
            text = "Testing Tools",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.simulateRandomData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Simulate Random Data")
            }
            
            Button(
                onClick = { viewModel.triggerFireAlert() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotOccupied
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Fire Alert")
            }
            
            OutlinedButton(
                onClick = { viewModel.resetAllSpots() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Spots")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Click on any parking spot to change its status (for testing only). In the next phase, the app will be connected to Bluetooth sensors to receive real-time data.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Error state content
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error Occurred",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Connection status banner showing Bluetooth connection info
 */
@Composable
private fun ConnectionStatusBanner(connectionState: ConnectionState) {
    if (connectionState is ConnectionState.Connected) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothConnected,
                    contentDescription = "Connected",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connected to ${connectionState.deviceName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Receiving live data",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50).copy(alpha = 0.7f)
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}
