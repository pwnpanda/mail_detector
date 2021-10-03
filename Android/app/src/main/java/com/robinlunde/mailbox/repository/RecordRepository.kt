package com.robinlunde.mailbox.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.*
import com.robinlunde.mailbox.network.ApiInterfaceRecord
import com.robinlunde.mailbox.network.HttpRequestLib2

class RecordRepository(val util: Util) : RepositoryInterface<Record> {
    override var data = MutableLiveData<MutableList<Record>>()
    private val logTag = "RecordRepository -"

    private var recordInterface: ApiInterfaceRecord = HttpRequestLib2.getClient(util).create(ApiInterfaceRecord::class.java)

    suspend fun getRecord(rec_id: Int): Record {
        val record =  recordInterface.getRecord(util.user!!.id!!, rec_id)
        if (findObject(record) == null)    addEntry(record)
        Log.d(logTag, "Found record $record in getRecord")
        return record
    }

    suspend fun getRecords(): MutableList<Record> {
        val records = recordInterface.getRecords(util.user!!.id!!)
        data.value?.clear()
        data.value = records
        Log.d(logTag, "Found records $records in getRecords")
        return records
    }

    suspend fun createRecord(day_id: Int, pill_id: Int, taken: Boolean = false): Record{
        val user_id = util.user!!.id!!
        var record = Record(day_id, user_id, pill_id, taken)
        record = recordInterface.createRecord(user_id, record)
        addEntry(record)
        //util.pillLogAdapter.notifyItemInserted(/* TODO find int in array of object */)
        Log.d(logTag, "Created record $record in createRecord")
        // Todo check if all pills are taken!
        return record
    }

    suspend fun updateRecord(record: Record): Record {
        findAndRemoveItemById(record.id!!)
        val newRecord =  recordInterface.updateRecord(util.user!!.id!!, record.id, record)
        addEntry(newRecord)
        //util.pillLogAdapter.notifyItemChanged(/* TODO find int in array of object */)
        Log.d(logTag, "Updated record $record in updateRecord")
        // Todo check if all pills are taken
        return record
    }

    suspend fun deleteRecord(rec_id: Int): ConcreteGenericType {
        findAndRemoveItemById(rec_id)
        Log.d(logTag, "Removed record ${find(rec_id)} in deleteRecord")
        val index = util.recordrepo.data.value!!.indexOf(util.recordrepo.find(rec_id)!!)
        util.pillLogAdapter.notifyItemRemoved( index )
        // Todo update all pills taken
        return recordInterface.deleteRecord(util.user!!.id!!, rec_id)
    }

    fun findRecordsByDay(day: Day): MutableList<Record>?{
        val rec: MutableList<Record> = mutableListOf()
        for ( item in data.value!! ){
            if (day == item.day)            rec.add(item)
            else if (day.id == item.day_id) rec.add(item)
        }
        if (rec.size == 0)  return null
        return rec
    }

    fun findRecordsByPill(pill: Pill): MutableList<Record>?{
        val rec: MutableList<Record> = mutableListOf()
        for ( item in data.value!! ){
            if (pill == item.pill)              rec.add(item)
            else if (pill.id == item.pill_id)   rec.add(item)
        }
        if (rec.size == 0)  return null
        return rec
    }

    fun findRecordsByUser(user: User): MutableList<Record>?{
        val rec: MutableList<Record> = mutableListOf()
        for ( item in data.value!! ){
            if (user == item.user)              rec.add(item)
            else if (user.id == item.user_id)   rec.add(item)
        }
        if (rec.size == 0)  return null
        return rec
    }

    fun areAllTaken(today: String): Boolean{
        val noActivePills = util.pillrepo.noActivePillsExists()
        val curDay = util.dayrepo.findByDate(today) ?: return noActivePills
        val records = findRecordsByDay(curDay)
        return records?.all { record -> record.taken } ?: noActivePills
    }

    fun getTakenColors(today: String): Unit? {
        val curDay = util.dayrepo.findByDate(today) ?: return null
        val records = findRecordsByDay(curDay) ?: return null
        return records.filter { record -> record.taken }.forEach { record -> record.pill!!.color }
    }
}