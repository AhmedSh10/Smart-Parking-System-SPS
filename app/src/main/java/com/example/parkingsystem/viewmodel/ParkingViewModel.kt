package com.example.parkingsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingsystem.bluetooth.BleDevice
import com.example.parkingsystem.bluetooth.BluetoothManager
import com.example.parkingsystem.bluetooth.ConnectionState
import com.example.parkingsystem.model.ParkingSpot
import com.example.parkingsystem.model.ParkingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for managing parking system state and Bluetooth connection
 * 
 * @property application Application context
 */
class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    // Bluetooth manager
    private val bluetoothManager = BluetoothManager(application)

    // Parking state
    private val _parkingState = MutableStateFlow<ParkingState>(ParkingState.Loading)
    val parkingState: StateFlow<ParkingState> = _parkingState

    // Bluetooth states
    val connectionState: StateFlow<ConnectionState> = bluetoothManager.connectionState
    val discoveredDevices: StateFlow<List<BleDevice>> = bluetoothManager.discoveredDevices

    // Flag to track if using real data
    private var usingRealData = false

    init {
        // Load initial data
        loadInitialData()

        // Listen for Bluetooth data
        viewModelScope.launch {
            bluetoothManager.parkingData.collect { data ->
                data?.let {
                    usingRealData = true
                    updateStateFromBluetooth(it)
                }
            }
        }
    }

    /**
     * Load initial parking data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _parkingState.value = ParkingState.Loading
            delay(1000) // Simulate loading

            val spots = (1..6).map { id ->
                ParkingSpot(id = id, isOccupied = false)
            }

            _parkingState.value = ParkingState.Success(
                spots = spots,
                fireAlertActive = false
            )
        }
    }

    /**
     * Update parking state from Bluetooth data
     * 
     * @param data Byte array containing sensor data
     * Format: [Spot1, Spot2, Spot3, Spot4, Spot5, Spot6, FireAlert]
     * Each byte: 0 = Available/No Fire, 1 = Occupied/Fire
     */
    private fun updateStateFromBluetooth(data: ByteArray) {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success && data.size >= 7) {
            val updatedSpots = currentState.spots.mapIndexed { index, spot ->
                if (index < 6) {
                    spot.copy(
                        isOccupied = data[index] == 1.toByte(),
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    spot
                }
            }

            val fireAlert = data[6] == 1.toByte()

            _parkingState.value = currentState.copy(
                spots = updatedSpots,
                fireAlertActive = fireAlert
            )
        }
    }

    /**
     * Refresh parking data
     */
    fun refreshData() {
        if (!usingRealData) {
            loadInitialData()
        }
    }

    /**
     * Update a specific parking spot status (for testing)
     */
    fun updateSpotStatus(spotId: Int, isOccupied: Boolean) {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success && !usingRealData) {
            val updatedSpots = currentState.spots.map { spot ->
                if (spot.id == spotId) {
                    spot.copy(
                        isOccupied = isOccupied,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    spot
                }
            }

            _parkingState.value = currentState.copy(spots = updatedSpots)
        }
    }

    /**
     * Simulate random parking data (for testing)
     */
    fun simulateRandomData() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success && !usingRealData) {
            val updatedSpots = currentState.spots.map { spot ->
                spot.copy(
                    isOccupied = Random.nextBoolean(),
                    lastUpdated = System.currentTimeMillis()
                )
            }

            _parkingState.value = currentState.copy(spots = updatedSpots)
        }
    }

    /**
     * Trigger fire alert (for testing)
     */
    fun triggerFireAlert() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success && !usingRealData) {
            _parkingState.value = currentState.copy(fireAlertActive = true)
        }
    }

    /**
     * Dismiss fire alert
     */
    fun dismissFireAlert() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success) {
            _parkingState.value = currentState.copy(fireAlertActive = false)
        }
    }

    /**
     * Reset all parking spots to available (for testing)
     */
    fun resetAllSpots() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success && !usingRealData) {
            val updatedSpots = currentState.spots.map { spot ->
                spot.copy(
                    isOccupied = false,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            _parkingState.value = currentState.copy(
                spots = updatedSpots,
                fireAlertActive = false
            )
        }
    }

    // Bluetooth functions

    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothManager.isBluetoothEnabled()
    }

    /**
     * Start scanning for Bluetooth devices
     */
    fun startBluetoothScan() {
        bluetoothManager.startScanning()
    }

    /**
     * Stop scanning for Bluetooth devices
     */
    fun stopBluetoothScan() {
        bluetoothManager.stopScanning()
    }

    /**
     * Connect to a Bluetooth device
     */
    fun connectToDevice(deviceAddress: String) {
        bluetoothManager.connectToDevice(deviceAddress)
    }

    /**
     * Disconnect from current Bluetooth device
     */
    fun disconnectBluetooth() {
        bluetoothManager.disconnect()
        usingRealData = false
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        bluetoothManager.cleanup()
    }
}
