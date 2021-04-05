package com.robinlunde.mailbox.network

import android.util.Log
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.robinlunde.mailbox.MailboxApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

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
                        Log.d("Bluetooth","Scan failed")
                        _scanStatus.value =
                            ScanStatus.Failed(cause.message ?: "Unknown error")
                    }
                    .onCompletion { cause ->
                        Log.d("Bluetooth", "scan over")
                        if (cause == null) _scanStatus.value =
                            ScanStatus.Stopped
                    }
                    .filter { it.isPi }
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
        Log.d("Bluetooth","Scan stopped")
        scanScope.cancelChildren()
        _scanStatus.value = ScanStatus.Stopped
    }

    fun btEnabledConfirmed() {
        Log.d("BlueTooth", "Bluetooth has been enabled")
        startScan()
    }
}

// How to identify correct node. TODO Need to update for different boards
private val Advertisement.isPi
    get() = name?.startsWith("RaspberryPi") == true ||
            name?.startsWith("Pi") == true || address == "B8:27:EB:12:D0:9A"

private fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

private fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)