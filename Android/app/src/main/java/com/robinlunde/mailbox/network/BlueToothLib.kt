package com.robinlunde.mailbox.network

import android.util.Log
import com.juul.kable.*
import com.robinlunde.mailbox.MailboxApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.internal.notify
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

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
    private val ack: ByteArray = byteArrayOf(0)
    private var attempt = AtomicInteger()


    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Stopped)
    private val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    private val advertisement = _advertisements.asStateFlow()

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
                        // If connection found, set it up
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                        Log.d("Bluetooth Vals received", _advertisements.value.toString())
                        Log.d("Bluetooth addr", advertisement.address)
                        handleContentHelper()
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
        scanScope.cancelChildren()
        _scanStatus.value = ScanStatus.Stopped
    }

    fun btEnabledConfirmed() {
        Log.d("BlueTooth", "Bluetooth has been enabled")
        startScan()
    }

    private fun handleContentHelper() {

        scanScope.launch {

            Log.d("BlueTooth", "Trying to handle content")
            val peripheral = scanScope.peripheral(advertisement.value[0])
            Log.d("Bluetooth", "Peripheral $peripheral created")
            peripheral.state
                .filter { it is State.Disconnected }
                .onEach {
                    // 0.5 sec * 2^(x)
                    val time: Long = (200L * 2f.pow( attempt.getAndIncrement() ) ).toLong()
                    Log.w("Bluetooth", "Not connected! Backoff-time is: $time (200 * 2^$attempt = 200 * ${2f.pow(attempt.get())})")
                    delay(time)
                    handleContent(peripheral)
                }
                .launchIn(this)
        }
    }

    private fun CoroutineScope.handleContent(peripheral: Peripheral) {
        launch {
            try {
                peripheral.connect()

                Log.d("Bluetooth", "Connected to $peripheral")
                peripheral.state.collect { state: State ->
                    Log.d("Bluetooth", "State changed to $state for peripheral $peripheral")

                    when(state) {
                        State.Connected -> {
                            attempt.set(0)
                            // TODO
                            // Send to API that we connected to IoT Device, and that nothing was received
                            //val content: String = runBlocking { withContext(Dispatchers.IO) { peripheral.read(dataConfigCharacteristics).decodeToString() } }
                            //Log.d("Bluetooth", "First data: $content")
                            // If there is data, send to API and to phone that we have new mail and that an alert should trigger
                            try {
                                peripheral.observe(dataConfigCharacteristics).collect { data ->
                                    Log.d("Bluetooth", "Data update: ${data.decodeToString()}")
                                    // TODO 1
                                    // Send message to API and trigger alert

                                    // Send message to BT device as ack
                                    runBlocking { withContext(Dispatchers.IO) {
                                        peripheral.write(dataConfigCharacteristics, ack, WriteType.WithoutResponse)
                                        peripheral.notify()
                                    } }

                                    peripheral.disconnect()
                                    stopScan()
                                }
                            } catch (e: Exception){
                                Log.d("Bluetooth", "Exception $e")
                            }
                        }

                        State.Connecting -> {
                            Log.d("Bluetooth", "Connecting")
                        }

                        State.Disconnecting -> {
                            Log.d("Bluetooth", "Disconnecting")

                        }

                        else -> Log.d("Bluetooth", "Disconnected")
                    }
                }

                // Catch weird connection lost issue
            } catch (e: ConnectionLostException) {
                Log.d("Bluetooth", "ConnectionLostException! Trying again after a short while!")
                return@launch
                // Catch a real exception
            } catch (e: Exception) {
                Log.d("Bluetooth", "Fatal exception $e occurred. Exiting!")
                throw e
            }
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