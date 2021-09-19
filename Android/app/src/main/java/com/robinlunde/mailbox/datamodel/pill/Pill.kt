package com.robinlunde.mailbox.datamodel.pill

import android.graphics.Color.parseColor
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.robinlunde.mailbox.MailboxApp
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO evaluate if I need to add more fields

class Pill(
    val color: String,
    val active: Boolean,
    private val created_at: String? = null,
    private var updated_at: String? = null,
    override val id: Int? = null,
    override val msg: String? = null
): GenericType<Pill>() {
    private lateinit var timestamp: String
    lateinit var uuid: UUID
    private var userid: Int? = null
    val colorRes = parseColor(color)
    // TODO Need to create colorRes from the color code
    //  val colorRes: ColorRes = color as ColorRes

    val pill: Pill = this
    val util = MailboxApp.getUtil()
    val name = getName(uuid)

    var user: User? = if (userid != null)  util.userCheck(userid) else null
    val date = if (::timestamp.isInitialized) util.getMyDate(timestamp) else ""
    val time = if (::timestamp.isInitialized) util.getMyTime(timestamp) else ""


    private fun reverseDate(str: String): String {
        return str.split("-").reversed().joinToString("-")
    }

    fun getName(uuid: UUID): String {
        // TODO Lookup UUID in sharedEncryptedPrefs, return associated value
        return ""
    }

    override fun get(): Pill {
        return this
    }
}
