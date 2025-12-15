package com.example.parkingsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkingsystem.ui.screens.BluetoothScanScreen
import com.example.parkingsystem.ui.screens.ParkingScreen
import com.example.parkingsystem.ui.screens.SplashScreen
import com.example.parkingsystem.ui.theme.ParkingSystemTheme
import com.example.parkingsystem.viewmodel.ParkingViewModel


/**
 * Main Activity for the Parking System application
 * 
 * This activity manages navigation between screens:
 * - Splash Screen
 * - Parking Screen (main)
 * - Bluetooth Scan Screen
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ParkingSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Main navigation composable
 */
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    val viewModel: ParkingViewModel = viewModel()
    
    when (currentScreen) {
        Screen.Splash -> {
            SplashScreen(
                onSplashFinished = { currentScreen = Screen.Parking }
            )
        }
        
        Screen.Parking -> {
            ParkingScreen(
                viewModel = viewModel,
                onBluetoothClick = { currentScreen = Screen.BluetoothScan }
            )
        }
        
        Screen.BluetoothScan -> {
            BluetoothScanScreen(
                viewModel = viewModel,
                onBackPressed = { currentScreen = Screen.Parking },
                onDeviceConnected = { currentScreen = Screen.Parking }
            )
        }
    }
}

/**
 * Enum representing app screens
 */
enum class Screen {
    Splash,
    Parking,
    BluetoothScan
}
