package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO evaluate if I need to add more fields

class Day(
    val today: String
): GenericType<Day>() {

    override fun get(): Day {
        return this
    }
}