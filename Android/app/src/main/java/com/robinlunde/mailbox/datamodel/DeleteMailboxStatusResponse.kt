package com.robinlunde.mailbox.datamodel

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// "message":"Post deleted"!
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteMailboxStatusResponse(val message: String) {
    @JsonIgnore
    val success = message == "Post deleted!"
}
