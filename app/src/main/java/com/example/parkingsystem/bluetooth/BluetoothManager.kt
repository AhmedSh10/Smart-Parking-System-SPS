package com.example.parkingsystem.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Manager class for handling all Bluetooth Low Energy (BLE) operations
 * 
 * This class manages:
 * - Scanning for BLE devices
 * - Connecting to HM-10 module
 * - Receiving data from sensors
 * - Managing connection state
 * 
 * @property context Application context
 */
@SuppressLint("MissingPermission")
class BluetoothManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothManager"
        
        // HM-10 Default UUIDs
        private val SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        private val CHARACTERISTIC_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
        private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    // Bluetooth adapter
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }

    // GATT connection
    private var bluetoothGatt: BluetoothGatt? = null
    
    // BLE Scanner
    private val bleScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    // State flows
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices

    private val _parkingData = MutableStateFlow<ByteArray?>(null)
    val parkingData: StateFlow<ByteArray?> = _parkingData

    // GATT Callback
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    val deviceName = gatt.device.name ?: "Unknown Device"
                    _connectionState.value = ConnectionState.Connected(
                        deviceName = deviceName,
                        deviceAddress = gatt.device.address
                    )
                    // Discover services
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionState.value = ConnectionState.Disconnected
                    _parkingData.value = null
                }
                else -> {
                    Log.d(TAG, "Connection state changed: $newState")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered successfully")
                
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        // Enable notifications
                        val success = gatt.setCharacteristicNotification(characteristic, true)
                        Log.d(TAG, "Notification enabled: $success")
                        
                        // Enable notification on the remote device
                        val descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                            Log.d(TAG, "Descriptor written successfully")
                        } else {
                            Log.e(TAG, "Descriptor not found")
                        }
                    } else {
                        Log.e(TAG, "Characteristic not found")
                        _connectionState.value = ConnectionState.Error("Characteristic not found")
                    }
                } else {
                    Log.e(TAG, "Service not found")
                    _connectionState.value = ConnectionState.Error("Service not found")
                }
            } else {
                Log.e(TAG, "Service discovery failed: $status")
                _connectionState.value = ConnectionState.Error("Service discovery failed")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value
            if (data != null && data.size >= 7) {
                Log.d(TAG, "Data received: ${data.contentToString()}")
                _parkingData.value = data
            } else {
                Log.w(TAG, "Invalid data received (expected 7 bytes): ${data?.contentToString()}")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                if (data != null && data.size >= 7) {
                    Log.d(TAG, "Data read: ${data.contentToString()}")
                    _parkingData.value = data
                }
            }
        }
    }

    // Scan callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val bleDevice = BleDevice(
                device = device,
                rssi = result.rssi
            )
            
            // Add to list if not already present
            val currentList = _discoveredDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == bleDevice.address }
            
            if (existingIndex >= 0) {
                // Update existing device (RSSI might have changed)
                currentList[existingIndex] = bleDevice
            } else {
                // Add new device
                currentList.add(bleDevice)
            }
            
            _discoveredDevices.value = currentList
            Log.d(TAG, "Device found: ${bleDevice.name} (${bleDevice.address}) RSSI: ${bleDevice.rssi}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            _connectionState.value = ConnectionState.Error("Scan failed: $errorCode")
        }
    }

    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Start scanning for BLE devices
     */
    fun startScanning() {
        if (!isBluetoothEnabled()) {
            _connectionState.value = ConnectionState.Error("Bluetooth is not enabled")
            return
        }

        try {
            _connectionState.value = ConnectionState.Scanning
            _discoveredDevices.value = emptyList()
            bleScanner?.startScan(scanCallback)
            Log.d(TAG, "Started scanning for BLE devices")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scan: ${e.message}")
            _connectionState.value = ConnectionState.Error("Failed to start scan: ${e.message}")
        }
    }

    /**
     * Stop scanning for BLE devices
     */
    fun stopScanning() {
        try {
            bleScanner?.stopScan(scanCallback)
            Log.d(TAG, "Stopped scanning")
            if (_connectionState.value is ConnectionState.Scanning) {
                _connectionState.value = ConnectionState.Disconnected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop scan: ${e.message}")
        }
    }

    /**
     * Connect to a BLE device by address
     */
    fun connectToDevice(deviceAddress: String) {
        try {
            stopScanning()
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            if (device != null) {
                _connectionState.value = ConnectionState.Connecting(device.name ?: "Unknown Device")
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
                Log.d(TAG, "Connecting to device: ${device.name} ($deviceAddress)")
            } else {
                _connectionState.value = ConnectionState.Error("Device not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}")
            _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
        }
    }

    /**
     * Disconnect from the current device
     */
    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            _connectionState.value = ConnectionState.Disconnected
            _parkingData.value = null
            Log.d(TAG, "Disconnected from device")
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopScanning()
        disconnect()
    }
}
