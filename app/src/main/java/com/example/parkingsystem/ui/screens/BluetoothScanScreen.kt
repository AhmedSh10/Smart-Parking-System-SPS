package com.example.parkingsystem.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkingsystem.bluetooth.ConnectionState
import com.example.parkingsystem.ui.components.DeviceListItem
import com.example.parkingsystem.viewmodel.ParkingViewModel

/**
 * Screen for scanning and connecting to BLE devices
 * 
 * @param viewModel The parking view model
 * @param onBackPressed Callback when back button is pressed
 * @param onDeviceConnected Callback when successfully connected to a device
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScanScreen(
    viewModel: ParkingViewModel,
    onBackPressed: () -> Unit,
    onDeviceConnected: () -> Unit
) {
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val context = LocalContext.current
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(context, "Permissions granted, starting scan...", Toast.LENGTH_SHORT).show()
            viewModel.startBluetoothScan()
        } else {
            Toast.makeText(context, "Bluetooth permissions required!", Toast.LENGTH_LONG).show()
        }
    }
    
    // Request permissions on first composition
    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }
    
    // Show toast messages for connection state changes
    LaunchedEffect(connectionState) {
        when (connectionState) {
            is ConnectionState.Connected -> {
                val deviceName = (connectionState as ConnectionState.Connected).deviceName
                Toast.makeText(context, "✅ Connected to $deviceName", Toast.LENGTH_LONG).show()
                onDeviceConnected()
            }
            is ConnectionState.Connecting -> {
                Toast.makeText(context, "⏳ Connecting...", Toast.LENGTH_SHORT).show()
            }
            is ConnectionState.Error -> {
                val message = (connectionState as ConnectionState.Error).message
                Toast.makeText(context, "❌ Error: $message", Toast.LENGTH_LONG).show()
            }
            is ConnectionState.Scanning -> {
                Toast.makeText(context, "🔍 Scanning for devices...", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bluetooth Devices",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopBluetoothScan()
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            viewModel.startBluetoothScan()
                            Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show()
                        },
                        enabled = connectionState !is ConnectionState.Scanning
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status card
            StatusCard(connectionState = connectionState)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Device list
            if (discoveredDevices.isEmpty() && connectionState is ConnectionState.Scanning) {
                // Scanning indicator
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
                            text = "Scanning for ALL Bluetooth devices...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Make sure Bluetooth is enabled",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else if (discoveredDevices.isEmpty()) {
                // No devices found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No devices found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap refresh to scan again",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // Device list header
                Text(
                    text = "Found ${discoveredDevices.size} device(s)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Devices
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discoveredDevices) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = {
                                Toast.makeText(context, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
                                viewModel.connectToDevice(device.device.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status card showing current connection state
 */
@Composable
private fun StatusCard(connectionState: ConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ConnectionState.Connected -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                is ConnectionState.Connecting -> Color(0xFF2196F3).copy(alpha = 0.1f)
                is ConnectionState.Scanning -> Color(0xFF2196F3).copy(alpha = 0.1f)
                is ConnectionState.Error -> Color(0xFFF44336).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(6.dp),
                    color = when (connectionState) {
                        is ConnectionState.Connected -> Color(0xFF4CAF50)
                        is ConnectionState.Connecting -> Color(0xFF2196F3)
                        is ConnectionState.Scanning -> Color(0xFF2196F3)
                        is ConnectionState.Error -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                ) {}
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Status text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (connectionState) {
                        is ConnectionState.Connected -> "Connected"
                        is ConnectionState.Connecting -> "Connecting..."
                        is ConnectionState.Scanning -> "Scanning..."
                        is ConnectionState.Disconnected -> "Disconnected"
                        is ConnectionState.Error -> "Error"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (connectionState) {
                        is ConnectionState.Connected -> Color(0xFF4CAF50)
                        is ConnectionState.Connecting -> Color(0xFF2196F3)
                        is ConnectionState.Scanning -> Color(0xFF2196F3)
                        is ConnectionState.Error -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (connectionState is ConnectionState.Connected) {
                    Text(
                        text = "Device: ${connectionState.deviceName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else if (connectionState is ConnectionState.Error) {
                    Text(
                        text = connectionState.message,
                        fontSize = 14.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}
