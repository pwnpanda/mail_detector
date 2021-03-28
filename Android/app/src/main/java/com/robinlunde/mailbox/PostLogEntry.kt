package com.robinlunde.mailbox

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostLogEntry( val id: Int, val username: String, val delivered: String, val pickup: String) {
        @JsonIgnore
        val util = MailboxApp.getUtil()
        var deliveredDate: String = reverseDate(util.getMyDate(delivered))
        var deliveredTime: String = util.getMyTime(delivered)
        var pickupDate: String = reverseDate(util.getMyDate(pickup))
        var pickupTime: String = util.getMyTime(pickup)

        private fun reverseDate(str: String): String{
                return str.split("-").reversed().joinToString("-")
        }

        // CAn change to ovveride toString
        public fun toMyString(): String {
                return "PostLogEntry: ID: $id, Username: $username, " +
                        "Delivered: $delivered, DeliveredTime: $deliveredTime, DeliveredDate: $deliveredDate, " +
                        "Pickup: $pickup, PickupTime: $pickupTime, PickupDate: $pickupDate"
                //return super.toString()
        }
}
