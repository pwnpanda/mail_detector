package com.robinlunde.mailbox.datamodel

import com.fasterxml.jackson.annotation.JsonIgnore
import com.robinlunde.mailbox.MailboxApp

class PostUpdateStatus(
    val newMail: Boolean,
    val timestamp: String? = null,
    val username: String
) {
    @JsonIgnore
    val util = MailboxApp.getUtil()
    val date = if (timestamp != "" && timestamp != null )   util.getMyDate(timestamp) else ""
    val time = if (timestamp != "" && timestamp != null ) util.getMyTime(timestamp) else ""
    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }

    override fun toString(): String {
        return "New mail arrived: $newMail. Last update at $timestamp by $username"
    }
}
