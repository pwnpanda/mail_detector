package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.robinlunde.mailbox.MailboxApp

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Record(
    val day_id: Int?,
    val user_id: Int?,
    val pill_id: Int?,
    val taken: Boolean = false,
    private val created_at: String? = null,
    private var updated_at: String? = null,
    override val id: Int? = null,
    override val msg: String? = null
): GenericType<Record>() {
    var updated: String = created_at ?: ""

    @JsonIgnore
    val util = MailboxApp.getUtil()
    @JsonIgnore
    val user = util.userCheck(user_id)
    @JsonIgnore
    val pill: Pill? = util.pillrepo.find(pill_id)
    @JsonIgnore
    val day: Day? = util.dayrepo.find(day_id)

    val updateDate = if (updated != "") util.getMyDate(updated) else ""
    val updateTime = if (updated != "") util.getMyTime(updated) else ""

    override fun get(): Record {
        return this
    }

    override fun toString(): String {
        var resString = "Day id: $day_id User id: $user_id Pill id: $pill_id Taken: $taken "
        if (created_at != null)   resString += "Created at: $created_at "
        if (updated_at != null)   resString += "Updated at: $updated_at "
        if (id != null)   resString += "ID: $id "
        if (msg != null)   resString += "msg: $msg "
        if (updated != "")   resString += "Updated at: $updated - Date: $updateDate Time: $updateTime "
        if (user != null)   resString += "User: $user "
        if (pill != null)   resString += "Pill: $pill "
        if (day != null)   resString += "User: $day "

        return resString
        //return super.toString()
    }
}