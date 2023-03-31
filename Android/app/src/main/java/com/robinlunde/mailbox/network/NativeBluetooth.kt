package com.robinlunde.mailbox.network

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothGatt.*
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.MainActivity
import com.robinlunde.mailbox.debug.ScanType
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

private val ESP_MAC = "7C:9E:BD:D9:E1:1A"
private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
private val CHARACTERISTIC_REAL_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
private val CHARACTERISTIC_DEBUG_UUID = UUID.fromString("0f06709f-e038-4f4f-8795-31c514ec22dd")

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

    private var localGatt: BluetoothGatt? = null

    private val ack: ByteArray = byteArrayOf(1)
    private var connected = false
    private val attempt = AtomicInteger()

    private lateinit var device: BluetoothDevice

    // Scan for 10 sec
    private val scanPeriod: Long = 10000

    @RequiresApi(Build.VERSION_CODES.S)
    public fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
            MailboxApp.getContext()!!,
            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                MainActivity.myActivity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                999
            )
        }

    }
    @RequiresApi(Build.VERSION_CODES.S)
    public fun connectToDevice(){
        checkPermission()

        if (connected)  {
            Timber.d("Device $device is already connected!")
            return
        }

        if (this::device.isInitialized) {
            Timber.d("Connect to previously found bonded device!")
            device.connectGatt(MailboxApp.getContext(), true, gattCallback, TRANSPORT_LE)
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices

        for(d: BluetoothDevice in pairedDevices) {
            // Timber.d("Device: ${d.address} - Target addr $ESP_MAC")
            if (d.address.equals(ESP_MAC)) {
                device = d
                Timber.d("Connecting to found bonded device!")
                d.connectGatt(MailboxApp.getContext(), true, gattCallback, TRANSPORT_LE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun btEnabledConfirmed() {
        //bleScan(ScanType.ACTIVE)
        checkPermission()
        connectToDevice()

    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun bleConnect(){
        checkPermission()
        connectToDevice()
    }

    // Scans for 10 sec, then relaunched 30 sec after
    @RequiresApi(Build.VERSION_CODES.S)
    fun bleScan(type: ScanType = ScanType.BACKGROUND) {
        checkPermission()
        // Timber.d("In scan")
        scanner.let { scanner ->
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    Timber.d("Scan over from async action!")
                    scanning = false
                    scanner.stopScan(leScanCallback)
                }, scanPeriod)

                scanning = true
                when (type) {
                    ScanType.ACTIVE -> {
                        Timber.d("Active scan started!")
                        scanner.startScan(scanFilterList, scanSettingsActive, leScanCallback)
                    }
                    ScanType.BACKGROUND -> {
                        Timber.d("Passive scan started!")
                        scanner.startScan(scanFilterList, scanSettingsBackground, leScanCallback)
                    }
                }
            } else {
                Timber.d("Scan over naturally!")
                scanning = false
                scanner.stopScan(leScanCallback)
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            checkPermission()
            Timber.d("Found some results!")
            // This result contains a lot of interesting data. May want to use it later
            device = result!!.device
            device.connectGatt(
                MailboxApp.getInstance().applicationContext,
                true,
                gattCallback,
                TRANSPORT_LE
            )
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.d("Scan failed! Retrying!")
            retryConnection()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun retryConnection() {
        // call again after exponential backoff
        val delay = 200F * 2.0.pow(attempt.getAndIncrement())

        try {
            Handler(Looper.myLooper()!!).postDelayed({
                //bleScan(ScanType.ACTIVE)
                bleConnect()
            }, delay.toLong())
        } catch (nullPointer: NullPointerException) {
            // Might want to try to detect exactly what is throwing null pointer!
            Timber.d("Caught NullPointerException in rescan function. Likely due to client disconnect. Stacktrace: " + nullPointer.localizedMessage)
        } catch (e: Exception) {
            Timber.d("Caught exception: " + e.printStackTrace() + " - Need to be handled?")
            throw e
        }
    }

    // Connect callback
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        // Detects and registers changes during the Connection-process for the GATT Server
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            checkPermission()
            // We succeeded in whatever we wanted to do
            if (status == GATT_SUCCESS) {
                localGatt = gatt
                // Check what is currently happening
                when (newState) {
                    // We're connection
                    STATE_CONNECTING -> {
                        Timber.d("Connecting to GATT server")
                    }

                    // Hell yeah connected!
                    STATE_CONNECTED -> {
                        // Set global connection variables
                        if (gatt.device == device) connected = true
                        attempt.set(0)
                        Timber.d("Connected to GATT server!")
                        handleGattConnection(gatt)
                        // Send timestamp to MailboxApp
                        // Use info to send new "last checked" API update
                        MailboxApp.newBTData(MailboxApp.getUtil().getTime(), onlyTimestamp = true)
                    }

                    // We're disconnecting
                    STATE_DISCONNECTING -> {
                        Timber.d("Disconnecting from GATT Server")
                    }

                    // We're disconnecting
                    STATE_DISCONNECTED -> {
                        // We disconnected. Close connection!
                        if (gatt.device == device)  connected = false
                        Timber.d("Disconnected from GATT server!")
                        gatt.close()
                    }
                    // We're doing something real weird!
                    else -> {
                        if (gatt.device == device)  connected = false
                        Timber.d("Unknown connection state: $newState!")
                    }
                }
                // Some error happened, need to handle it!
            } else {
                Timber.d("An error occurred. Error: $status")

                if (status == GATT_FAILURE) {
                    Timber.w("GATT_FAILURE error: Code $status")
                } else {
                    Timber.w("Other error: Code $status")
                }

                // Close connection due to error
                gatt.close()
                // Retry connection
                retryConnection()
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            checkPermission()
            if (status == GATT_SUCCESS) {
                // continue
                val gattServices = gatt!!.services
                Timber.d("Discovered " + gattServices.size + " services for " + device.name)
                // Check that the service exists
                for (service in gattServices) {
                    if (SERVICE_UUID == service.uuid) {
                        // Service exists, lets check characteristic
                        for (characteristic in service.characteristics) {
                            if (CHARACTERISTIC_REAL_UUID == characteristic.uuid) {
                                // Characteristic exists, lets have fun
                                // Get notified if characteristic changes / has a value
                                gatt.setCharacteristicNotification(characteristic, true)
                            } else if (CHARACTERISTIC_DEBUG_UUID == characteristic.uuid) {
                                // Register for debug data
                                gatt.setCharacteristicNotification(characteristic, true)
                            }
                        }
                    }
                }

                // Periodically read the remote connection value - every 5 sec
                try {
                    Handler(Looper.getMainLooper()).also {
                        it.post(object : Runnable {
                            override fun run() {
                                checkPermission()
                                gatt.readRemoteRssi()
                                it.postDelayed(this, 5000)
                            }
                        })
                    }
                } catch (e: Exception) {
                    Timber.w("Exception $e happened!")
                    Timber.w(e.printStackTrace().toString())
                }

            } else {
                Timber.d("OnServiceDiscovered returned status: $status")
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            checkPermission()
            Timber.d("Characteristic " + characteristic!!.uuid + " changed. New value: " + characteristic.value.decodeToString())
            if (characteristic.uuid == CHARACTERISTIC_REAL_UUID) {
                val valFromSensor = characteristic.value.decodeToString()
                // Send ack
                characteristic.value = ack
                gatt!!.writeCharacteristic(characteristic)
                // Notify app and api of new mail
                MailboxApp.newBTData(valFromSensor, onlyTimestamp = false)
                // TODO disconnect
                // gatt.disconnect()
            } else if (characteristic.uuid == CHARACTERISTIC_DEBUG_UUID) {
                Timber.d("Received data: " + characteristic.value.decodeToString())
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
                Timber.d("Read from characteristic " + characteristic!!.uuid + ". Status: " + status + " - Success. Value: " + characteristic.value.decodeToString())
            } else {
                Timber.d("Read from characteristic " + characteristic!!.uuid + ". Status: " + status + " - Failure")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == GATT_SUCCESS) {
                Timber.d("Write to characteristic " + characteristic!!.uuid + ". Status: " + status + " - Success!")
            } else {
                Timber.d("Write to characteristic " + characteristic!!.uuid + ". Status: " + status + " - Failure")
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == GATT_SUCCESS) {
                Timber.i("Signal for remote server $rssi")
                MailboxApp.setRSSIData(rssi)
            } else {
                Timber.d("Getting signal for remote server failed with status: $status. Remote server is gone!")
            }
            super.onReadRemoteRssi(gatt, rssi, status)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun handleGattConnection(gatt: BluetoothGatt) {
        checkPermission()
        // Need to check bondState!
        when (device.bondState) {
            BOND_NONE -> {
                Timber.d("Not bonded, continue with service discovery")
                gatt.discoverServices()
            }

            BOND_BONDING -> {
                Timber.d("Currently bonding! Wait for it to complete to continue!")
            }

            BOND_BONDED -> {
                Timber.d("Bonded with service, continue with service discovery")
                gatt.discoverServices()
            }

            else -> {
                Timber.d("Unknown bond state: " + device.bondState)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun requestDebugData() {
        checkPermission()
        val debugService = localGatt?.getService(SERVICE_UUID)
        val debugCharacteristic = debugService?.getCharacteristic(
            CHARACTERISTIC_DEBUG_UUID
        )
        debugCharacteristic?.value = ack
        localGatt?.writeCharacteristic(debugCharacteristic)
    }
}
/**
 * Call disconnect when connection is over (when data has been sent to device)
 * Then the callback will handle close() call
 *
 */