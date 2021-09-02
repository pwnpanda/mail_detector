package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.GenericType


interface RepositoryInterface<T : GenericType<T>> {
    val data: MutableLiveData<MutableList<T>>

    fun find(id: Int?): T? {
        if (id == null) return null
        for ( found in data.value!! ){
            if (id == found.id) return found
        }
        return null
    }

    fun all(): MutableLiveData<MutableList<T>> {
        return data
    }

    fun size(): Int {
        return data.value!!.size
    }
}