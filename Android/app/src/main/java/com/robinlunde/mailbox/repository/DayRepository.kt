package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.Day
import com.robinlunde.mailbox.network.ApiInterfaceDay
import com.robinlunde.mailbox.network.HttpRequestLib2
import retrofit2.Retrofit
import timber.log.Timber

class DayRepository(val util: Util) : RepositoryInterface<Day> {
    override var data = MutableLiveData<MutableList<Day>>()

    private val client = getClient(HttpRequestLib2.getClient(util))
    private var dayInterface: ApiInterfaceDay = client.create(ApiInterfaceDay::class.java)

    private fun getClient(client: Retrofit): Retrofit {
        Timber.d("Client: $client")
        return client
    }

    suspend fun getDay(day_id: Int): Day {
        val day = dayInterface.getDay(util.user!!.id!!, day_id)
        if (findObject(day) == null) addEntry(day)
        Timber.d("Found day $day in getDay")
        return day
    }

    suspend fun getDays(): MutableList<Day> {
        Timber.d("User: " + util.user)
        Timber.d("$dayInterface")
        val days = dayInterface.getDays(util.user!!.id!!)
        data.value?.clear()
        data.value = days
        Timber.d( "Found days ${days.joinToString(" - ")} in getDays")
        return days
    }

    suspend fun createDay(today: String): Day {
        var day = Day(today)
        day = dayInterface.createDay(util.user!!.id!!, day)
        Timber.d("Created day $day in createDay")
        addEntry(day)
        return day
    }

    suspend fun updateDay(day: Day): Day {
        findAndRemoveItemById(day.id!!)
        val newDay = dayInterface.updateDay(util.user!!.id!!, day.id, day)
        addEntry(newDay)
        Timber.d("Updated day $day in updateDay")
        return day
    }

    suspend fun deleteDay(day_id: Int): ConcreteGenericType {
        findAndRemoveItemById(day_id)
        Timber.d("Removed day " + find(day_id) + " in deleteDay")
        return dayInterface.deleteDay(util.user!!.id!!, day_id)
    }

    fun findByDate(today: String): Day? {
        for (day in data.value!!){
            if (day.today == today) return day
        }
        return null
    }
}