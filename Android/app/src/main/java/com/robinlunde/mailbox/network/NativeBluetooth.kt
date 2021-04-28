package com.robinlunde.mailbox.network

import android.bluetooth.*
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothGatt.*
import android.bluetooth.le.*
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.debug.ScanType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
private val CHARACTERISTIC_REAL_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
private val CHARACTERISTIC_DEBUG_UUID = UUID.fromString("")

class NativeBluetooth {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.myLooper()!!)
    private val scanFilter =
        ScanFilter.Builder().setDeviceName("FireBeetle ESP32-E Robin").setServiceUuid(
            ParcelUuid(SERVICE_UUID)
        ).build()
    private val scanFilterList = listOf<ScanFilter>(scanFilter)
    private val scanSettingsActive =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()
    private val scanSettingsBackground =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_OPPORTUNISTIC)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()

    private val ack: ByteArray = byteArrayOf(1)
    private var connected = false
    private val attempt = AtomicInteger()

    private lateinit var device: BluetoothDevice
    private val logTag = "Bluetooth_native"

    // Scan for 10 sec
    private val scanPeriod: Long = 10000

    fun btEnabledConfirmed() {
        bleScan(ScanType.ACTIVE)
    }

    fun bleScan(type: ScanType = ScanType.BACKGROUND) {
        scanner.let { scanner ->
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    Log.d(logTag, "Scan over from async action!")
                    scanning = false
                    scanner.stopScan(leScanCallback)
                }, scanPeriod)

                scanning = true
                when (type) {
                    ScanType.ACTIVE -> {
                        Log.d(logTag, "Active scan started!")
                        scanner.startScan(scanFilterList, scanSettingsActive, leScanCallback)
                    }
                    ScanType.BACKGROUND -> {
                        Log.d(logTag, "Passive scan started!")
                        scanner.startScan(scanFilterList, scanSettingsBackground, leScanCallback)
                    }
                }
            } else {
                Log.d(logTag, "Scan over naturally!")
                scanning = false
                scanner.stopScan(leScanCallback)
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            // This result contains a lot of interesting data. May want to use it later
            device = result!!.device
            device.connectGatt(
                MailboxApp.getInstance().applicationContext,
                true,
                gattCallback,
                TRANSPORT_LE
            )
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(logTag, "Scan failed! Retrying!")
            retryConnection()
        }
    }

    private fun retryConnection() {
        // call again after exponential backoff
        val delay = 200F * 2.0.pow(attempt.getAndIncrement())

        try {
            Handler(Looper.myLooper()!!).postDelayed({
                bleScan(ScanType.ACTIVE)
            }, delay.toLong())
        } catch (nullPointer: NullPointerException) {
            // Might want to try to detect exactly what is throwing null pointer!
            Log.d(logTag, "Caught NullPointerException in rescan function. Likely due to client disconnect. Stacktrace: ${nullPointer.printStackTrace()}")
        } catch (e: Exception) {
            Log.d(logTag,"Caught exception: ${e.printStackTrace()} - Need to be handled?")
            throw e
        }
    }

    // Connect callback
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        // Detects and registers changes during the Connection-process for the GATT Server
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // We succeeded in whatever we wanted to do
            if (status == GATT_SUCCESS) {
                // Check what is currently happening
                when (newState) {
                    // We're connection
                    STATE_CONNECTING -> {
                        Log.d(logTag, "Connecting to GATT server")
                    }

                    // Hell yeah connected!
                    STATE_CONNECTED -> {
                        // Set global connection variables
                        connected = true
                        attempt.set(0)
                        Log.d(logTag, "Connected to GATT server!")
                        handleGattConnection(gatt)
                        // TODO
                        // Send timestamp to MailboxApp
                        // Use info to send new "last checked" API update
                    }

                    // We're disconnecting
                    STATE_DISCONNECTING -> {
                        Log.d(logTag, "Disconnecting from GATT Server")
                    }

                    // We're disconnecting
                    STATE_DISCONNECTED -> {
                        // We disconnected. Close connection!
                        connected = false
                        Log.d(logTag, "Disconnected from GATT server!")
                        gatt.close()
                    }
                    // We're doing something real weird!
                    else -> {
                        Log.d(logTag, "Unknown connection state: $newState!")
                    }
                }
                // Some error happened, need to handle it!
            } else {
                Log.d(logTag, "An error occurred. Error: $status")

                if (status == GATT_FAILURE) {
                    Log.w(logTag, "GATT_FAILURE error: Code $status")
                } else {
                    Log.w(logTag, "Other error: Code $status")
                }

                // Close connection due to error
                gatt.close()
                // Retry connection
                retryConnection()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == GATT_SUCCESS) {
                // continue
                val gattServices = gatt!!.services
                Log.d(logTag, "Discovered ${gattServices.size} services for ${device.name}")
                // Check that the service exists
                for (service in gattServices) {
                    if (SERVICE_UUID == service.uuid) {
                        // Service exists, lets check characteristic
                        for (characteristic in service.characteristics) {
                            if (CHARACTERISTIC_REAL_UUID == characteristic.uuid) {
                                // Characteristic exists, lets have fun
                                // Get notified if characteristic changes / has a value
                                gatt.setCharacteristicNotification(characteristic, true)
                            } else if (CHARACTERISTIC_DEBUG_UUID == characteristic.uuid){
                                // Register for debug data
                                gatt.setCharacteristicNotification(characteristic, true)
                            }
                        }
                    }
                }

                // Periodically read the remote connection value - every 30 sec
                try {
                    Handler(Looper.getMainLooper()).also {
                        it.post(object : Runnable{
                            override fun run() {
                                gatt.readRemoteRssi()
                                it.postDelayed(this, 30000)
                            }
                        })
                    }
                } catch (e: Exception) {
                    Log.w(logTag, "Exception $e happened!")
                    Log.w(logTag, e.printStackTrace().toString())
                }

            } else {
                Log.d(logTag, "OnServiceDiscovered returned status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d(
                logTag,
                "Characteristic ${characteristic!!.uuid} changed. New value: ${characteristic.value.decodeToString()}"
            )
            if (characteristic.uuid == CHARACTERISTIC_REAL_UUID){
                val difference = characteristic.value.decodeToString()
                // Send ack
                characteristic.value = ack
                gatt!!.writeCharacteristic(characteristic)
                // Notify app and api of new mail
                MailboxApp.newBTData(difference)
                // TODO disconnect
            } else if (characteristic.uuid == CHARACTERISTIC_DEBUG_UUID) {
                MailboxApp.setSensorData(characteristic.value.decodeToString().toDouble())
            }
        }

        // Unused, but useful to store in case needed later
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == GATT_SUCCESS) {
                Log.d(
                    logTag,
                    "Read from characteristic ${characteristic!!.uuid}. Status: $status - Success. Value: ${characteristic.value.decodeToString()}"
                )
            } else {
                Log.d(
                    logTag,
                    "Read from characteristic ${characteristic!!.uuid}. Status: $status - Failure"
                )
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == GATT_SUCCESS) {
                Log.d(
                    logTag,
                    "Write to characteristic ${characteristic!!.uuid}. Status: $status - Success!"
                )
            } else {
                Log.d(
                    logTag,
                    "Write to characteristic ${characteristic!!.uuid}. Status: $status - Failure"
                )
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == GATT_SUCCESS) {
                Log.i(logTag, "Signal for remote server $rssi")
            } else {
                Log.d(
                    logTag,
                    "Getting signal for remote server failed with status: $status. Remote server is gone!"
                )
            }
            super.onReadRemoteRssi(gatt, rssi, status)
        }
    }

    private fun handleGattConnection(gatt: BluetoothGatt) {
        // Need to check bondState!
        when (device.bondState) {
            BOND_NONE -> {
                Log.d(logTag, "Not bonded, continue with service discovery")
                gatt.discoverServices()
            }

            BOND_BONDING -> {
                Log.d(logTag, "Currently bonding! Wait for it to complete to continue!")
            }

            BOND_BONDED -> {
                Log.d(logTag, "Bonded with service, continue with service discovery")
                gatt.discoverServices()
            }

            else -> {
                Log.d(logTag, "Unknown bond state: ${device.bondState}")
            }
        }
    }
}
/**
 * Call disconnect when connection is over (when data has been sent to device)
 * Then the callback will handle close() call
 *
 */