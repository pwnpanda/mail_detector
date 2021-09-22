package com.robinlunde.mailbox.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.Record
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
        Log.d(logTag, "Created record $record in createRecord")
        return record
    }

    suspend fun updateRecord(record: Record): Record {
        findAndRemoveItemById(record.id!!)
        val newRecord =  recordInterface.updateRecord(util.user!!.id!!, record.id, record)
        addEntry(newRecord)
        Log.d(logTag, "Updated record $record in updateRecord")
        return record
    }

    suspend fun deleteRecord(rec_id: Int): GenericType<Record> {
        findAndRemoveItemById(rec_id)
        Log.d(logTag, "Removed record ${find(rec_id)} in deleteRecord")
        return recordInterface.deleteRecord(util.user!!.id!!, rec_id)
    }
    /**
    To be used for guidance

    val allTaken: Boolean = isAllTaken(allRecords)

    private fun isAllTaken(records: Array<Record>): Boolean {
    return records.all { record -> record.taken }
    }

    fun getTakenColors() {
    return allRecords.filter { record -> record.taken }.forEach { record -> record.color }
    }
     */
}