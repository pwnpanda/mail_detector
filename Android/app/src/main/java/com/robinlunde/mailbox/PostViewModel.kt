package com.robinlunde.mailbox

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PostViewModel (savedStateHandle: SavedStateHandle) : ViewModel() {

    val postId : Int = savedStateHandle["id"] ?: throw IllegalArgumentException("Missing post ID")

    val postEntries: MutableLiveData<MutableList<PostLogEntry>> by lazy {
        MutableLiveData<MutableList<PostLogEntry>>()
    }
}