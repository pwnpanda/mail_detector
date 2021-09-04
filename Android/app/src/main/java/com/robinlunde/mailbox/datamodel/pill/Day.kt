package com.robinlunde.mailbox.datamodel.pill

class Day(
    val today: String
): GenericType<Day>() {

    override fun get(): Day {
        return this
    }
}