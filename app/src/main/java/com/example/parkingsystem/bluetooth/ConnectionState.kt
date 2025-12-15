package com.example.parkingsystem.bluetooth

/**
 * Sealed class representing the connection state of the Bluetooth connection
 */
sealed class ConnectionState {
    /**
     * Not connected to any device
     */
    object Disconnected : ConnectionState()
    
    /**
     * Scanning for available devices
     */
    object Scanning : ConnectionState()
    
    /**
     * Attempting to connect to a device
     */
    data class Connecting(val deviceName: String) : ConnectionState()
    
    /**
     * Successfully connected to a device
     */
    data class Connected(val deviceName: String, val deviceAddress: String) : ConnectionState()
    
    /**
     * Connection failed or error occurred
     */
    data class Error(val message: String) : ConnectionState()
    
    /**
     * Get a user-friendly status message
     */
    fun getStatusMessage(): String {
        return when (this) {
            is Disconnected -> "Disconnected"
            is Scanning -> "Scanning for devices..."
            is Connecting -> "Connecting to $deviceName..."
            is Connected -> "Connected to $deviceName"
            is Error -> "Error: $message"
        }
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return this is Connected
    }
}
