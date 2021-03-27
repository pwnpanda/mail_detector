package com.robinlunde.mailbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//@entity(tableName = "post_entries_table")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PostEntry(
        // TODO needs util functions to generate better timestamps
        //@PrimaryKey(autoGenerate = true)
        val id: Int,
        val username: String,
        val delivered: String,
        val pickup: String,
        val deliveredDay: String,
        val deliveredTime: String,
        val pickupDay: String,
        val pickupTime: String
        ) {
        // Constructor should autofill as is
        // Todo it should also split up timestamps by splitting on "T"
        fun splitData(input: String): String {
                return ""
        }
}
