package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.robinlunde.mailbox.MailboxApp
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Pill(
    var color: Int,
    var active: Boolean,
    private val created_at: String? = null,
    private var updated_at: String? = null,
    var uuid: UUID? = null,
    override val id: Int? = null,
    override val msg: String? = null
): GenericType<Pill>() {
    private var timestamp: String = created_at ?: ""
    private var userid: Int? = null
    @JsonIgnore
    val util = MailboxApp.getUtil()
    var name = getPillName()

    @JsonIgnore
    var user: User? = if (userid != null)  util.userCheck(userid) else null

    val date = if (timestamp != "") util.getMyDate(timestamp) else ""
    val time = if (timestamp != "") util.getMyTime(timestamp) else ""


    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }

    fun getPillName(): String {
        val prefs = MailboxApp.getPrefs()
        var name = ""
        if (uuid != null) name = prefs.getString(uuid.toString(), "") ?: ""
        // Timber.d("Pill with $uuid has name: $name")
        return name
    }

    override fun get(): Pill {
        return this
    }

    override fun toString(): String {
        var resString = "Color: $color Active: $active "
        if (name != "")   resString = "Name: $name $resString"
        if (created_at != null)   resString += "Created at: $created_at "
        if (updated_at != null)   resString += "Updated at: $updated_at "
        if (uuid != null) resString += "UUID: $uuid "
        if (id != null)   resString += "ID: $id "
        if (msg != null)   resString += "msg: $msg "
        if (userid != null)   resString += "Userid: $userid "
        if (timestamp != "")   resString += "Timestamp: $timestamp - Date: $date Time: $time "
        if (user != null)   resString += "User: $user "

        return resString
    }
}
