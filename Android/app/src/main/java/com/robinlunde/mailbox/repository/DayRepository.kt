package com.robinlunde.mailbox.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.Day
import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.network.ApiInterfaceDay

class DayRepository(val util: Util) : RepositoryInterface<Day> {
    override var data = MutableLiveData<MutableList<Day>>()
    private val logTag = "DayRepository -"

    private var dayInterface: ApiInterfaceDay = util.http2.create(ApiInterfaceDay::class.java)

    suspend fun getDay(day_id: Int): Day {
        val day =  dayInterface.getDay(util.user!!.id!!, day_id)
        if (findObject(day) == null)    addEntry(day)
        Log.d(logTag, "Found day $day in getDay")
        return day
    }

    suspend fun getDays(): MutableList<Day> {
        val days = dayInterface.getDays(util.user!!.id!!)
        data.value?.clear()
        data.value = days
        Log.d(logTag, "Found days $days in getDays")
        return days
    }

    suspend fun updateDay(day: Day): Day {
        findAndRemoveItemById(day.id!!)
        val newDay =  dayInterface.updateDay(util.user!!.id!!, day.id, day)
        addEntry(newDay)
        Log.d(logTag, "Updated day $day in updatePill")
        return day
    }

    suspend fun deleteDay(day_id: Int): GenericType<Day> {
        findAndRemoveItemById(day_id)
        Log.d(logTag, "Removed day ${find(day_id)} in deleteDay")
        return dayInterface.deleteDay(util.user!!.id!!, day_id)
    }
}