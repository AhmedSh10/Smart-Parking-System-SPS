package com.example.parkingsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingsystem.model.ParkingSpot
import com.example.parkingsystem.model.ParkingState

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing parking system state
 * 
 * This ViewModel handles:
 * - Parking spots status (available/occupied)
 * - Fire alert system
 * - Data simulation for testing
 * - Future: Bluetooth communication with sensors
 */
class ParkingViewModel : ViewModel() {
    
    // Private mutable state
    private val _parkingState = MutableStateFlow<ParkingState>(ParkingState.Loading)
    
    // Public immutable state for UI
    val parkingState: StateFlow<ParkingState> = _parkingState.asStateFlow()
    
    init {
        // Initialize with 5 parking spots (all available)
        initializeParkingSpots()
    }
    
    /**
     * Initialize parking spots with default values
     */
    private fun initializeParkingSpots() {
        viewModelScope.launch {
            // Simulate loading delay
            delay(1000)
            
            val spots = List(5) { index ->
                ParkingSpot(
                    id = index + 1,
                    isOccupied = false
                )
            }
            
            _parkingState.value = ParkingState.Success(
                spots = spots,
                fireAlertActive = false
            )
        }
    }
    
    /**
     * Update status of a specific parking spot
     * 
     * @param spotId ID of the spot to update (1-5)
     * @param isOccupied New occupation status
     */
    fun updateSpotStatus(spotId: Int, isOccupied: Boolean) {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success) {
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
     * Trigger fire alert
     */
    fun triggerFireAlert() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success) {
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
     * Simulate random parking data for testing
     * This will be replaced with actual Bluetooth data later
     */
    fun simulateRandomData() {
        viewModelScope.launch {
            val currentState = _parkingState.value
            if (currentState is ParkingState.Success) {
                val updatedSpots = currentState.spots.map { spot ->
                    spot.copy(
                        isOccupied = (0..1).random() == 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                
                _parkingState.value = currentState.copy(spots = updatedSpots)
            }
        }
    }
    
    /**
     * Refresh parking data
     * Future: This will fetch data from Bluetooth/sensors
     */
    fun refreshData() {
        viewModelScope.launch {
            // Simulate refresh delay
            delay(500)
            
            // For now, just update timestamps
            val currentState = _parkingState.value
            if (currentState is ParkingState.Success) {
                val updatedSpots = currentState.spots.map { spot ->
                    spot.copy(lastUpdated = System.currentTimeMillis())
                }
                _parkingState.value = currentState.copy(spots = updatedSpots)
            }
        }
    }
    
    /**
     * Reset all parking spots to available
     */
    fun resetAllSpots() {
        val currentState = _parkingState.value
        if (currentState is ParkingState.Success) {
            val updatedSpots = currentState.spots.map { spot ->
                spot.copy(
                    isOccupied = false,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            _parkingState.value = currentState.copy(spots = updatedSpots)
        }
    }
}
