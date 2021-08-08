package com.robinlunde.mailbox.datamodel

import com.robinlunde.mailbox.MailboxApp

class PillDay (
    private val timestamp: String,
    val allPills: Array<SinglePill>,
){
    val util = MailboxApp.getUtil()
    val date = util.getMyDate(timestamp)
    val time = util.getMyTime(timestamp)
    val allTaken: Boolean = isAllTaken(allPills)

    private fun isAllTaken(pills: Array<SinglePill>): Boolean{
        return pills.all { pill -> pill.taken }
    }

    public fun getTakenColors(){
        return allPills.filter{ pill -> pill.taken }.forEach{ pill -> pill.color }
    }
}