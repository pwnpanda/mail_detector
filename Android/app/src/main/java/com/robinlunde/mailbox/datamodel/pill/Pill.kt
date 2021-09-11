package com.robinlunde.mailbox.datamodel.pill

import androidx.annotation.ColorRes
import com.fasterxml.jackson.annotation.JsonInclude
import com.robinlunde.mailbox.MailboxApp
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class Pill(
    val color: ColorRes,
    val active: Boolean
): GenericType<Pill>() {
    private lateinit var timestamp: String
    lateinit var uuid: UUID
    private var userid: Int? = null


    val pill: Pill = this
    val util = MailboxApp.getUtil()
    val name = getName(uuid)

    var user: User? = if (userid != null)  util.userCheck(userid) else null
    val date = if (::timestamp.isInitialized) util.getMyDate(timestamp) else ""
    val time = if (::timestamp.isInitialized) util.getMyTime(timestamp) else ""


    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }

    fun getName(uuid: UUID): String {
        // TODO Lookup UUID in sharedEncryptedPrefs, return associated value
        return ""
    }

    override fun get(): Pill {
        return this
    }
}
