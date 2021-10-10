package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.GenericType
import timber.log.Timber


interface RepositoryInterface<T : GenericType<T>> {
    val data: MutableLiveData<MutableList<T>>

    fun find(id: Int?): T? {
        if (id == null) return null
        for (item in data.value!!) {
            if (id == item.id) return item
        }
        return null
    }

    fun findObject(obj: T?): T? {
        if (obj == null) return null
        for (item in data.value!!) {
            if (item == obj) return obj
        }
        return null
    }

    fun all(): MutableLiveData<MutableList<T>> {
        return data
    }

    fun size(): Int {
        return data.value!!.size
    }

    fun addEntry(obj: T) {
        val update = data.value
        update?.add(obj)
        data.postValue(update)
    }

    fun findAndRemoveItemByObject(obj: T) {
        val update = data.value
        update?.remove(obj)
        data.postValue(update)
    }

    fun deleteItem(obj: T) {
        val index = findObject(obj)
        data.value!!.remove(obj)
    }

    // Returns true if removed
    fun findAndRemoveItemById(obj_id: Int): Boolean {
        val item = find(obj_id)
        if (item != null) {
            val update = data.value
            update?.remove(item)
            data.postValue(update)
            Timber.d("Found item with ID: $obj_id")
        } else {
            Timber.d("Did not find item with ID: $obj_id")
        }
        return item != null
    }
}