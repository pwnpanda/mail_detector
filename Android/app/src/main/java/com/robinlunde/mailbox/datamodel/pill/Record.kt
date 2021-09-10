package com.robinlunde.mailbox.datamodel.pill

import com.robinlunde.mailbox.MailboxApp

class Record(
    day_id: Int?,
    user_id: Int?,
    pill_id: Int?,
    val taken: Boolean

): GenericType<Record>() {
    lateinit var updated: String

    val util = MailboxApp.getUtil()
    val user = util.userCheck(user_id)
    val pill: Pill? = util.pillrepo.find(pill_id)
    val day: Day? = util.dayrepo.find(day_id)

    val updateDate = if (::updated.isInitialized) util.getMyDate(updated) else ""
    val updateTime = if (::updated.isInitialized) util.getMyTime(updated) else ""

    override fun get(): Record {
        return this
    }

}