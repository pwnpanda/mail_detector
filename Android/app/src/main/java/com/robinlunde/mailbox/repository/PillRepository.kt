package com.robinlunde.mailbox.repository

import androidx.lifecycle.MutableLiveData
import com.robinlunde.mailbox.datamodel.pill.Pill

class PillRepository() : RepositoryInterface<Pill> {
    override var data = MutableLiveData<MutableList<Pill>>()
}