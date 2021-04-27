package com.robinlunde.mailbox.debug

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robinlunde.mailbox.MailboxApp

class DebugViewModel: ViewModel() {
    val sensorData = MutableLiveData<MutableList<Float>>()

    init {
        sensorData.value = MailboxApp.getSensorData()
    }
}