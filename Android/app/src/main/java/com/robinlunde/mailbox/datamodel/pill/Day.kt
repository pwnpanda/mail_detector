package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class Day(
    val today: String
): GenericType<Day>() {

    override fun get(): Day {
        return this
    }
}