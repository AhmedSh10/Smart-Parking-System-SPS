package com.example.parkingsystem.model

/**
 * Data class representing a single parking spot
 * 
 * @property id Unique identifier for the parking spot (1-5)
 * @property isOccupied Whether the spot is currently occupied
 * @property lastUpdated Timestamp of last status update (in milliseconds)
 */
data class ParkingSpot(
    val id: Int,
    val isOccupied: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Returns the status as a readable string
     */
    fun getStatusText(): String {
        return if (isOccupied) "Occupied" else "Available"
    }
}
