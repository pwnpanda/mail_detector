package com.robinlunde.mailbox

import android.app.Application
import androidx.lifecycle.MutableLiveData

class PostViewModel (dataSource: List<PostLogEntry>, application: Application) {
    // Reference to current entry
    private val currentPost = MutableLiveData<PostLogEntry?>()
    // Reference to all entries
    val postEntries = dataSource

}