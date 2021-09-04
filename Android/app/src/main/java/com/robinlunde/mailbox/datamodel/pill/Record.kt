package com.robinlunde.mailbox.datamodel.pill

import com.robinlunde.mailbox.MailboxApp

class Record(
    day_id: Int?,
    user_id: Int?,
    pill_id: Int?,
    val taken: Boolean,
    updated: String?
): GenericType<Record>() {
    val util = MailboxApp.getUtil()
    val updateDate = util.getMyDate(updated!!)
    val updateTime = util.getMyTime(updated!!)
    val user = util.userCheck(user_id)
    val pill: Pill? = util.pillrepo.find(pill_id)
    val day: Day? = util.dayrepo.find(day_id)

    override fun get(): Record {
        return this
    }

}