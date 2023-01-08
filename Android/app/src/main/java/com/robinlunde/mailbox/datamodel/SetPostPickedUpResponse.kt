package com.robinlunde.mailbox.datamodel

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.robinlunde.mailbox.MailboxApp

// {"id":12,"username":"Robin2","delivered":"2021-03-23T22:53:16.686Z","pickup":"2023-01-08T14:30:54.495Z","created_at":"2023-01-08T13:30:58.275Z","updated_at":"2023-01-08T13:30:58.275Z"}
@JsonIgnoreProperties(ignoreUnknown = true)
data class SetPostPickedUpResponse(
    val id: Int,
    val username: String,
    val delivered: String,
    val pickup: String,
    val created_at: String,
    val updated_at: String
){
    @JsonIgnore
    val util = MailboxApp.getUtil()

    // How to?
    // val success = username == MailboxApp.getUsername()

    var deliveredDate: String = reverseDate(util.getMyDate(delivered))
    var deliveredTime: String = util.getMyTime(delivered)
    var pickupDate: String = reverseDate(util.getMyDate(pickup))
    var pickupTime: String = util.getMyTime(pickup)

    val createdAtDate: String = reverseDate(util.getMyDate(created_at))
    val createdAtTime: String = util.getMyTime(created_at)
    val updatedAtDate: String = reverseDate(util.getMyDate(updated_at))
    val updatedAtTime: String = util.getMyTime(updated_at)

    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }
}
