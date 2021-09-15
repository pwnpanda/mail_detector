package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.robinlunde.mailbox.MailboxApp

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO evaluate if I need to add more fields
class Record(
    day_id: Int?,
    user_id: Int?,
    pill_id: Int?,
    val taken: Boolean,
    private val created_at: String? = null,
    private var updated_at: String? = null,
    override val id: Int? = null,
    override val msg: String? = null
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