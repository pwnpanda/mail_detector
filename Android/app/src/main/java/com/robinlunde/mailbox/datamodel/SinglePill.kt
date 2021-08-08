package com.robinlunde.mailbox.datamodel

import androidx.annotation.ColorRes
import com.robinlunde.mailbox.MailboxApp
import java.util.*

/**
 * Considerations for DB:
 * All pills need a start date, so it only applies from then
 * All pills need a boolean ACTIVE
 * UUID should be unique
 * Name should be local only
 */

class SinglePill (
    val id : UUID,
    val taken : Boolean,
    val color: ColorRes,
    private val timestamp: String
    ){

    val util = MailboxApp.getUtil()
    val date = util.getMyDate(timestamp)
    val time = util.getMyTime(timestamp)
    val name = getName(id)

    private fun reverseDate(str:String): String {
        return str.split("-").reversed().joinToString("-")
    }

    fun getName(uuid:UUID) : String {
        // TODO Lookup UUID in sharedEncryptedPrefs, return associated value
        return ""
    }
}
