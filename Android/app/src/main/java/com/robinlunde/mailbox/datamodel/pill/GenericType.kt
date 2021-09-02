package com.robinlunde.mailbox.datamodel.pill

interface GenericType<T> {
    val id: Int?
    fun get() : T
}