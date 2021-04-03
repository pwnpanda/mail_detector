package com.robinlunde.mailbox.logview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.datamodel.PostLogEntry

// https://medium.com/@atifmukhtar/recycler-view-with-mvvm-livedata-a1fd062d2280
class PostViewModel : ViewModel() {

    val mutablePostEntries: MutableLiveData<MutableList<PostLogEntry>> by lazy {
        MutableLiveData<MutableList<PostLogEntry>>()
    }

    init {
        mutablePostEntries.value = MailboxApp.getPostEntries()
    }
}