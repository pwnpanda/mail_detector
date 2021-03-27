package com.robinlunde.mailbox

import android.app.Application
import android.content.Context

class MailboxApp: Application() {

    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        username = this.getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE).getString(getString(R.string.username_pref),"").toString()
        util = Util(applicationContext)
        postLogEntryList = util.getLogs()
    }

    companion object{
        private lateinit var util: Util
        private lateinit var postLogEntryList: MutableList<PostLogEntry>
        private lateinit var mailboxApp: MailboxApp
        private lateinit var username: String
        fun getUsername(): String {
            return username
        }
        fun getInstance(): MailboxApp {
            return mailboxApp
        }
        fun getPostEntries(): MutableList<PostLogEntry> {
            return postLogEntryList
        }
        fun getUtil(): Util {
            return util
        }
    }
}