package com.robinlunde.mailbox.datamodel

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.robinlunde.mailbox.MailboxApp

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostLogEntry(
    val id: Int,
    val username: String,
    val delivered: String,
    val pickup: String
) {
    @JsonIgnore
    val util = MailboxApp.getUtil()
    var deliveredDate: String = reverseDate(util.getMyDate(delivered))
    var deliveredTime: String = util.getMyTime(delivered)
    var pickupDate: String = reverseDate(util.getMyDate(pickup))
    var pickupTime: String = util.getMyTime(pickup)

    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }

    // Can change to override toString
    /*
    fun toMyString(): String {
        return "PostLogEntry: ID: $id, Username: $username, " +
                "Delivered: $delivered, DeliveredTime: $deliveredTime, DeliveredDate: $deliveredDate, " +
                "Pickup: $pickup, PickupTime: $pickupTime, PickupDate: $pickupDate"
    }*/
}
