package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Day(
    val today: String,
    private val created_at: String? = null,
    private var updated_at: String? = null,
    override val id: Int? = null,
    override val msg: String? = null,
) : GenericType<Day>() {

    override fun get(): Day {
        return this
    }

    override fun toString(): String {
        var str = ""
        if (id != null)  str += "ID: $id "
        if (today != "")  str += "Today: $today "
        if (created_at != null)  str += "Created at: $created_at "
        if (updated_at != null)  str += "Updated at: $updated_at "
        if (msg != null)  str += "Message: $msg "

        if (str == "") return super.toString()

        return str
    }
}