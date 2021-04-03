package com.robinlunde.mailbox.alert

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.datamodel.PostUpdateStatus

class AlertViewModel : ViewModel() {
    val currentStatus = MutableLiveData<PostUpdateStatus>()

    init {
        currentStatus.value = MailboxApp.getStatus()
    }
}