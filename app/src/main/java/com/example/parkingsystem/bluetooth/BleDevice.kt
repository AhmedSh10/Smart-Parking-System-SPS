package com.example.parkingsystem.bluetooth

import android.bluetooth.BluetoothDevice

/**
 * Data class representing a discovered BLE device
 * 
 * @property device The Android BluetoothDevice object
 * @property name The device name (or "Unknown Device" if null)
 * @property address The MAC address of the device
 * @property rssi Signal strength (RSSI value)
 */
data class BleDevice(
    val device: BluetoothDevice,
    val name: String = run {
        try {
            device.name ?: "Unknown Device"
        } catch (_: SecurityException) {
            "Unknown Device"
        }
    },
    val address: String = run {
        try {
            device.address
        } catch (_: SecurityException) {
            "Unknown"
        }
    },
    val rssi: Int = 0
) {
    /**
     * Check if this device is likely the parking system
     */
    fun isParkingSystem(): Boolean {
        return name.contains("SmartParking", ignoreCase = true) ||
               name.contains("HMSoft", ignoreCase = true) ||
               name.contains("Parking", ignoreCase = true)
    }
    
    /**
     * Get signal strength as a percentage
     */
    fun getSignalStrength(): Int {
        return when {
            rssi >= -50 -> 100
            rssi >= -60 -> 80
            rssi >= -70 -> 60
            rssi >= -80 -> 40
            rssi >= -90 -> 20
            else -> 10
        }
    }
    
    /**
     * Get signal quality description
     */
    fun getSignalQuality(): String {
        return when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good"
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Weak"
            else -> "Very Weak"
        }
    }
}
