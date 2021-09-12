package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class User (
    val username: String? = null,
    var password: String? = null,
    var token: String? = null,
    id: Int? = null,
    msg: String? = null
): GenericType<User>() {

    override fun get(): User {
        return this
    }

    override fun toString(): String {
        var temp = ""

        if (id != null) temp += "ID $id"
        if (username != null)  temp += "Username $username "
        if (password != null)  temp += "Password $password "
        if (token != null)  temp += "Token $token "
        if (msg != null)  temp += "Message $msg "

        if (temp == "") temp = super.toString()

        return temp
    }
}