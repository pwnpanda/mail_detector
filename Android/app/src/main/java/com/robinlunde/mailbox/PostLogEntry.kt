package com.robinlunde.mailbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostLogEntry(
        val id: Int,
        val username: String,
        val delivered: String,
        val pickup: String,
        ){
        var deliveredDate: String = reverseDate(delivered.split("T")[0])
        var deliveredTime: String = delivered.split("T")[1].subSequence(0, 8).toString()
        var pickupDate: String = reverseDate(pickup.split("T")[0])
        var pickupTime: String = pickup.split("T")[1].subSequence(0, 8).toString()

        private fun reverseDate(str: String): String{
                return str.split("-").reversed().joinToString("-")
        }
        override fun toString(): String {
                return "PostLogEntry: ID: $id, Username: $username, " +
                        "Delivered: $delivered, DeliveredTime: $deliveredTime, DeliveredDate: $deliveredDate, " +
                        "Pickup: $pickup, PickupTime: $pickupTime, PickupDate: $pickupDate"
                //return super.toString()
        }
}
