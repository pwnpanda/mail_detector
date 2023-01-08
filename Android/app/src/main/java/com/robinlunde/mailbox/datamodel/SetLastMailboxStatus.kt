package com.robinlunde.mailbox.datamodel

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SetLastMailboxStatus(val message: String){
    val success = message == "Status stored!"
}
