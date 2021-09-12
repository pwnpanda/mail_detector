package com.robinlunde.mailbox.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.Pill
import com.robinlunde.mailbox.network.ApiInterfacePill

class PillRepository(val util: Util) : RepositoryInterface<Pill> {
    override var data = MutableLiveData<MutableList<Pill>>()
    private val logTag = "PillRepository -"


    private var pillInterface: ApiInterfacePill = this.util.http2.create(ApiInterfacePill::class.java)

    suspend fun getPill(pill_id: Int): Pill {
        val pill =  pillInterface.getPill(util.user!!.id!!, pill_id)
        if (findObject(pill) == null)    addEntry(pill)
        Log.d(logTag, "Found pill $pill in getPill")
        return pill
    }

    suspend fun getPills(): MutableList<Pill> {
        val pills = pillInterface.getPills(util.user!!.id!!)
        data.value?.clear()
        data.value = pills
        Log.d(logTag, "Found pills $pills in getPills")
        return pills
    }

    suspend fun updatePill(pill: Pill): Pill {
        findAndRemoveItemById(pill.id!!)
        val newPill =  pillInterface.updatePill(util.user!!.id!!, pill.id, pill)
        addEntry(newPill)
        Log.d(logTag, "Updated pill $pill in updatePills")
        return pill
    }

    suspend fun deletePill(pill_id: Int): GenericType<Pill> {
        findAndRemoveItemById(pill_id)
        Log.d(logTag, "Removed pill ${find(pill_id)} in deletePill")
        return pillInterface.deletePill(util.user!!.id!!, pill_id)
    }
}