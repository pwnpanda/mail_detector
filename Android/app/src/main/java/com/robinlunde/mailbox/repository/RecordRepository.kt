package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.*
import com.robinlunde.mailbox.network.ApiInterfaceRecord
import com.robinlunde.mailbox.network.HttpRequestLib2
import timber.log.Timber

class RecordRepository(val util: Util) : RepositoryInterface<Record> {
    override var data = MutableLiveData<MutableList<Record>>()

    private var recordInterface: ApiInterfaceRecord =
        HttpRequestLib2.getClient(util).create(ApiInterfaceRecord::class.java)

    suspend fun getRecord(rec_id: Int): Record {
        val record = recordInterface.getRecord(util.user!!.id!!, rec_id)
        if (findObject(record) == null) addEntry(record)
        Timber.d("Found record $record in getRecord")
        return record
    }

    suspend fun getRecords(): MutableList<Record> {
        val records = recordInterface.getRecords(util.user!!.id!!)
        data.value?.clear()
        data.value = records
        Timber.d("Found records $records in getRecords")
        return records
    }

    suspend fun createRecord(day_id: Int, pill_id: Int, taken: Boolean = false): Record {
        val user_id = util.user!!.id!!
        var record = Record(day_id, user_id, pill_id, taken)
        record = recordInterface.createRecord(user_id, record)
        addEntry(record)
        Timber.d("Created record $record in createRecord")
        return record
    }

    suspend fun updateRecord(record: Record): Record {
        findAndRemoveItemById(record.id!!)
        val newRecord = recordInterface.updateRecord(util.user!!.id!!, record.id, record)
        addEntry(newRecord)
        Timber.d("Updated record $record in updateRecord")
        return record
    }

    suspend fun deleteRecord(rec_id: Int): ConcreteGenericType {
        Timber.d("Removing record " + find(rec_id) + " in deleteRecord")
        val index = util.recordrepo.data.value!!.indexOf(util.recordrepo.find(rec_id)!!)
        if (!findAndRemoveItemById(rec_id)) {
            Timber.d("Item not found - cannot remove it!")
            return ConcreteGenericType()
        }
        util.pillLogAdapter.notifyItemRemoved(index)
        return recordInterface.deleteRecord(util.user!!.id!!, rec_id)
    }

    fun findRecordsByDay(day: Day): MutableList<Record>? {
        val rec: MutableList<Record> = mutableListOf()
        if (data.value == null) return null
        for (item in data.value!!) {
            if (day == item.day) rec.add(item)
            else if (day.id == item.day_id) rec.add(item)
        }
        if (rec.size == 0) return null
        return rec
    }

    fun findRecordsByPill(pill: Pill): MutableList<Record>? {
        val rec: MutableList<Record> = mutableListOf()
        for (item in data.value!!) {
            if (pill == item.pill) rec.add(item)
            else if (pill.id == item.pill_id) rec.add(item)
        }
        if (rec.size == 0) return null
        return rec
    }

    fun findRecordsByUser(user: User): MutableList<Record>? {
        val rec: MutableList<Record> = mutableListOf()
        for (item in data.value!!) {
            if (user == item.user) rec.add(item)
            else if (user.id == item.user_id) rec.add(item)
        }
        if (rec.size == 0) return null
        return rec
    }

    fun areAllTaken(today: String): Boolean {
        // Check if any pills are active
        val noActivePills = util.pillrepo.noActivePillsExists()
        Timber.d("No active pills: $noActivePills")
        // If we cannot find a day for today, return true (all pills taken) if there are no active pills. Otherwise return false
        val curDay = util.dayrepo.findByDate(today) ?: return noActivePills
        Timber.d("Today: $curDay")
        // If no records exists, return true (all pills taken) if there are no active pills. Otherwise return false
        val records = findRecordsByDay(curDay) ?: return noActivePills
        Timber.d("Records:")
        records.map {r -> Timber.d("record - $r")}
        // Confirm all active pills have records for the day
        if (util.pillrepo.activePills() != records.size)    return noActivePills
        return records.all { record -> record.taken }
    }

    fun getTakenColors(today: String): MutableList<Int>? {
        // Get colors for all pills taken unless there are no days or records found
        val curDay = util.dayrepo.findByDate(today) ?: return null
        val records = findRecordsByDay(curDay) ?: return null
        val colors = mutableListOf<Int>()
        records.filter { record -> record.taken }
            .forEach { record -> colors.add(record.pill!!.color) }
        return colors
    }
}