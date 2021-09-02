package com.robinlunde.mailbox.datamodel.pill

class User (
    override val id: Int?,
    val username: String,
    private val password: String,
    val token: String?
): GenericType<User> {

    override fun get(): User {
        return this
    }
}