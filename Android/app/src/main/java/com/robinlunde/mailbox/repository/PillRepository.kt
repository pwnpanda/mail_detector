package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.Pill
import com.robinlunde.mailbox.network.ApiInterfacePill
import com.robinlunde.mailbox.network.HttpRequestLib2
import timber.log.Timber

class PillRepository(val util: Util) : RepositoryInterface<Pill> {
    override var data = MutableLiveData<MutableList<Pill>>()


    private var pillInterface: ApiInterfacePill =
        HttpRequestLib2.getClient(util).create(ApiInterfacePill::class.java)

    suspend fun getPill(pill_id: Int): Pill {
        val pill = pillInterface.getPill(util.user!!.id!!, pill_id)
        if (findObject(pill) == null) addEntry(pill)
        Timber.d("Found pill $pill in getPill")
        return pill
    }

    suspend fun getPills(): MutableList<Pill> {
        val pills = pillInterface.getPills(util.user!!.id!!)
        data.value?.clear()
        data.postValue(pills)
        Timber.d("Found pills $pills in getPills")
        return pills
    }

    suspend fun createPill(color: Int, active: Boolean = false): Pill {
        var pill = Pill(color, active)
        Timber.d("Created temporary pill $pill in createPills")
        pill = pillInterface.createPill(util.user!!.id!!, pill)
        Timber.d("Send pill to API")
        addEntry(pill)
        util.pillUpdateAdapter.notifyItemInserted(data.value!!.size)
        Timber.d("Created pill $pill in createPills")
        return pill

    }

    suspend fun updatePill(pill: Pill): Pill {
        val index = data.value!!.indexOf(pill)
        val newPill = pillInterface.updatePill(util.user!!.id!!, pill.id!!, pill)
        data.value!![index] = newPill
        data.postValue(data.value!!)
        util.pillUpdateAdapter.notifyItemChanged(index)
        Timber.d("Updated pill $pill in updatePills at position $index\nNew pill: $newPill")
        return newPill
    }

    suspend fun deletePill(pill_id: Int): ConcreteGenericType {
        val index = data.value!!.indexOf(find(pill_id)!!)
        Timber.d("Removed pill " + find(pill_id) + " in deletePill at position " + index)
        findAndRemoveItemById(pill_id)
        util.pillUpdateAdapter.notifyItemRemoved(index)
        return pillInterface.deletePill(util.user!!.id!!, pill_id)
    }

    fun noActivePillsExists(): Boolean {
        return data.value!!.none { pill -> pill.active }
    }

    fun activePills(): Int {
        return data.value!!.count { pill -> pill.active }
    }
}