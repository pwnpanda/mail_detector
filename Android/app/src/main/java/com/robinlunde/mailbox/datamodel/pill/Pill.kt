package com.robinlunde.mailbox.datamodel.pill

import androidx.annotation.ColorRes
import com.robinlunde.mailbox.MailboxApp
import java.util.*

class Pill(
    val uuid: UUID?,
    val color: ColorRes,
    val active: Boolean,
    private val timestamp: String?,
    private val userid: Int?,
): GenericType<Pill>() {

    val util = MailboxApp.getUtil()
    val date = util.getMyDate(timestamp!!)
    val time = util.getMyTime(timestamp!!)
    val name = uuid?.let { getName(it) } ?: ""
    val user: User? = util.userCheck(userid)
    val pill: Pill = this

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
