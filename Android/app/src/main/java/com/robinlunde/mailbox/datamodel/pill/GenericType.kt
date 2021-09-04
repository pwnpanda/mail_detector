package com.robinlunde.mailbox.datamodel.pill

abstract class GenericType<T> {
    open val id: Int? = null
    open val msg: String? = null
    abstract fun get() : T
}