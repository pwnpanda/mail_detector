package com.robinlunde.mailbox.network

import android.util.Log
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.robinlunde.mailbox.MailboxApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)
private val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
private val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
private val dataConfigCharacteristics = characteristicOf(SERVICE_UUID, CHARACTERISTIC_UUID)

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Started : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}


/**
 * https://github.com/JuulLabs/sensortag/blob/14dc04c1aa7eafdbcb6c2bbbc4ec063f9276c42e/app/src/androidMain/kotlin/features/scan/ScanViewModel.kt
 * https://github.com/JuulLabs/sensortag
 * https://github.com/JuulLabs/kable
 * https://github.com/android/connectivity-samples/tree/main/BluetoothLeChat
 * https://github.com/android/connectivity-samples/tree/main/BluetoothLeGatt
 * https://developer.android.com/guide/topics/connectivity/use-ble
 */
class BlueToothLib {

    private val scanner = Scanner()
    private val scanScope = MailboxApp.getAppScope().childScope()
    private val found = hashMapOf<String, Advertisement>()


    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Stopped)
    val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisement = _advertisements.asStateFlow()

    fun startScan() {
        Log.d("Bluetooth", "Starting BT Scan")
        Log.d("Bluetooth", "ScanStatus ${scanStatus.value}")

        if (_scanStatus.value == ScanStatus.Started) return // Scan already in progress.
        _scanStatus.value = ScanStatus.Started

        scanScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner
                    .advertisements
                    .catch { cause ->
                        Log.d("Bluetooth", "Scan failed")
                        _scanStatus.value = ScanStatus.Failed(cause.message ?: "Unknown error")
                    }
                    .onCompletion { cause ->
                        Log.d("Bluetooth", "scan over")
                        if (cause == null) _scanStatus.value =
                            ScanStatus.Stopped
                    }
                        // Only keep the relevant data
                    .filter { it.isFireBeetle }
                        // If data found, keep data and stop scan
                        // TODO - Store value, notify UI and API of value (status) and Send response
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                        Log.d("Bluetooth Vals received", _advertisements.value.toString())
                        Log.d("Bluetooth addr", advertisement.address)
                        stopScan()
                    }
                /*.collect { advertisement ->
                     found[advertisement.address] = advertisement
                     _advertisements.value = found.values.toList()
                     Log.d("Bluetooth vals", _advertisements.value.toString())
                     Log.d("Bluetooth addr", advertisement.address) }
                  */
            }
        }
    }

    private fun stopScan() {
        Log.d("Bluetooth", "Scan stopped")
        handleContent()
        //scanScope.cancelChildren()
        _scanStatus.value = ScanStatus.Stopped
    }

    fun btEnabledConfirmed() {
        Log.d("BlueTooth", "Bluetooth has been enabled")
        startScan()
    }

    private fun handleContent() {
        Log.d("BlueTooth", "Trying to handle content")
        val peripheral = scanScope.peripheral(advertisement.value[0])
        Log.d("Bluetooth", "Peripheral $peripheral created")
        scanScope.launch {
            peripheral.connect()
            Log.d("BlueTOOTH", "Connected to $peripheral")

            val data: String = runBlocking { withContext(Dispatchers.IO) { peripheral.read(dataConfigCharacteristics).decodeToString() } }
            Log.d("Bluetooth", "First data: $data")

            try {
                peripheral.observe(dataConfigCharacteristics).collect { data ->
                    Log.d("Bluetooth", "Data update: ${data.decodeToString()}")
                }
            } catch (e: Exception){
                 Log.d("Bluetooth", "Exception $e")
             }
           
            /*peripheral.state.collect { state: State ->
                Log.d("Bluetooth", "State changed to $state for peripheral $peripheral")
            }*/
        }
    }
}

// How to identify correct node. TODO Need to update for different boards
private val Advertisement.isPi
    get() = name?.startsWith("RaspberryPi") == true ||
            name?.startsWith("Pi") == true || address == "B8:27:EB:12:D0:9A"

// How to identify correct node. TODO Need to update for different boards
private val Advertisement.isFireBeetle
    get() = name?.startsWith("FireBeetle") == true ||
            name?.startsWith("Robin") == true || address == "7C:9E:BD:D9:E1:92"

private fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

private fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)