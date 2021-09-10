package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
class User (
    val username: String,
    val password: String,
): GenericType<User>() {
    var token: String? = null

    override fun get(): User {
        return this
    }

    override fun toString(): String {
        return "User: $username"
        //return super.toString()
    }
}