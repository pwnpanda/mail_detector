package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.Record

class RecordRepository() : RepositoryInterface<Record> {
    override var data = MutableLiveData<MutableList<Record>>()
    /**
    To be used for guidance

    val allTaken: Boolean = isAllTaken(allPills)

    private fun isAllTaken(pills: Array<Pill>): Boolean {
    return pills.all { pill -> pill.taken }
    }

    fun getTakenColors() {
    return allPills.filter { pill -> pill.taken }.forEach { pill -> pill.color }
    }
     */
}