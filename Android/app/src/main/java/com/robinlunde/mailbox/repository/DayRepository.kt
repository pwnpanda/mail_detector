package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.Day

class DayRepository : RepositoryInterface<Day> {
    override var data = MutableLiveData<MutableList<Day>>()
}