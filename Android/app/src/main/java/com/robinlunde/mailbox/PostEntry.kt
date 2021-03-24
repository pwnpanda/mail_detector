package com.robinlunde.mailbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//@entity(tableName = "post_entries_table")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PostEntry(
        //@PrimaryKey(autoGenerate = true)
        val id: Int,
        val username: String,
        val delivered: String,
        val pickup: String
        )
