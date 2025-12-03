package com.example.parkingsystem.model

/**
 * Sealed class representing different states of the parking system
 */
sealed class ParkingState {
    /**
     * Loading state - initial state when app starts
     */
    object Loading : ParkingState()
    
    /**
     * Success state - parking data is available
     * 
     * @property spots List of all parking spots
     * @property fireAlertActive Whether fire alert is currently active
     */
    data class Success(
        val spots: List<ParkingSpot>,
        val fireAlertActive: Boolean = false
    ) : ParkingState() {
        /**
         * Returns count of available spots
         */
        fun getAvailableCount(): Int = spots.count { !it.isOccupied }
        
        /**
         * Returns count of occupied spots
         */
        fun getOccupiedCount(): Int = spots.count { it.isOccupied }
        
        /**
         * Returns total number of spots
         */
        fun getTotalCount(): Int = spots.size
    }
    
    /**
     * Error state - something went wrong
     * 
     * @property message Error message to display
     */
    data class Error(val message: String) : ParkingState()
}
