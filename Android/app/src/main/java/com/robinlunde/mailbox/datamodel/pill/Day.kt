package com.robinlunde.mailbox.datamodel.pill

class Day(
    override val id: Int?,
    val today: String,
): GenericType<Day> {

    override fun get(): Day {
        return this
    }
}