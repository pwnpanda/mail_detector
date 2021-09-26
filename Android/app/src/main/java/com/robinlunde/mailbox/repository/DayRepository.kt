package com.robinlunde.mailbox.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.Day
import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.network.ApiInterfaceDay
import com.robinlunde.mailbox.network.HttpRequestLib2
import retrofit2.Retrofit

class DayRepository(val util: Util) : RepositoryInterface<Day> {
    override var data = MutableLiveData<MutableList<Day>>()
    private val logTag = "DayRepository -"

    private val client = getClient(HttpRequestLib2.getClient(util))
    private var dayInterface: ApiInterfaceDay = client.create(ApiInterfaceDay::class.java)

    private fun getClient(client: Retrofit): Retrofit {
        Log.d("$logTag GetClient", "Client: $client")
        return client
    }

    suspend fun getDay(day_id: Int): Day {
        val day = dayInterface.getDay(util.user!!.id!!, day_id)
        if (findObject(day) == null) addEntry(day)
        Log.d(logTag, "Found day $day in getDay")
        return day
    }

    suspend fun getDays(): MutableList<Day> {
        Log.d("$logTag getDays", "User: ${util.user}")
        Log.d("$logTag getDays", "$dayInterface")
        val days = dayInterface.getDays(util.user!!.id!!)
        data.value?.clear()
        data.value = days
        Log.d(logTag, "Found days ${days.map { day -> day.toString() }} in getDays")
        return days
    }

    suspend fun createDay(today: String): Day {
        var day = Day(today)
        day = dayInterface.createDay(util.user!!.id!!, day)
        Log.d(logTag, "Created day $day in createDay")
        addEntry(day)
        return day
    }

    suspend fun updateDay(day: Day): Day {
        findAndRemoveItemById(day.id!!)
        val newDay = dayInterface.updateDay(util.user!!.id!!, day.id, day)
        addEntry(newDay)
        Log.d(logTag, "Updated day $day in updateDay")
        return day
    }

    suspend fun deleteDay(day_id: Int): ConcreteGenericType {
        findAndRemoveItemById(day_id)
        Log.d(logTag, "Removed day ${find(day_id)} in deleteDay")
        return dayInterface.deleteDay(util.user!!.id!!, day_id)
    }
}