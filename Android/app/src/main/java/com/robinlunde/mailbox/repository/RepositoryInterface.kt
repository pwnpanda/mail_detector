package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.GenericType


interface RepositoryInterface<T : GenericType<T>> {
    val data: MutableLiveData<MutableList<T>>

    fun find(id: Int?): T? {
        if (id == null) return null
        for ( item in data.value!! ){
            if (id == item.id) return item
        }
        return null
    }

    fun findObject(obj: T?): T?{
        if (obj == null)    return null
        for (item in data.value!!){
            if (item == obj)    return obj
        }
        return null
    }

    fun all(): MutableLiveData<MutableList<T>> {
        return data
    }

    fun size(): Int {
        return data.value!!.size
    }

    fun addEntry(obj: T){
        val update = data.value
        update?.add(obj)
        data.postValue(update)
    }

    private fun findAndRemoveItemByObject(obj: T){
        val update = data.value
        update?.remove(obj)
        data.postValue(update)
    }

    fun findAndRemoveItemById(obj_id: Int) {
        val item = find(obj_id)
        if (item != null){
            val update = data.value
            update?.remove(item)
            data.postValue(update)
        }
    }
}