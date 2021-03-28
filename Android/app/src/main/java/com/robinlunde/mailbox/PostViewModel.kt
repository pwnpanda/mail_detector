package com.robinlunde.mailbox

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// https://medium.com/@atifmukhtar/recycler-view-with-mvvm-livedata-a1fd062d2280
class PostViewModel() : ViewModel() {

    val mutablePostEntries: MutableLiveData<MutableList<PostLogEntry>> by lazy {
        MutableLiveData<MutableList<PostLogEntry>>()
    }
    private var postEntries: MutableList<PostLogEntry> = MailboxApp.getPostEntries()

    init {
        mutablePostEntries.value = postEntries
    }

    fun getPostEntries(): MutableLiveData<MutableList<PostLogEntry>> {
        return mutablePostEntries
    }

    fun setPostEntries(data: MutableList<PostLogEntry>) {
        postEntries = data
        mutablePostEntries.value = postEntries
    }

}