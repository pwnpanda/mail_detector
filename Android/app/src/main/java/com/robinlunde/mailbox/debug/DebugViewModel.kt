package com.robinlunde.mailbox.debug

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DebugViewModel : ViewModel() {
    val sensorData = MutableLiveData<MutableList<Double>>()
    val rssi = MutableLiveData(0)

    init {
        sensorData.value = mutableListOf(0.0)
    }
}