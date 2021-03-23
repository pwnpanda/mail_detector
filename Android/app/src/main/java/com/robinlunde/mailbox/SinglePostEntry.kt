package com.robinlunde.mailbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SinglePostEntry(val username: String, val delivered: String, val pickup: String)
