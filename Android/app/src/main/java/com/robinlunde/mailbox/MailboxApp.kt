package com.robinlunde.mailbox

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class MailboxApp: Application() {

    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        prefs = this.getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE)
        username = prefs.getString(getString(R.string.username_pref),"").toString()
        util = Util(applicationContext)
        postLogEntryList = util.getLogs()
    }

    companion object{
        private lateinit var util: Util
        private lateinit var postLogEntryList: MutableList<PostLogEntry>
        private lateinit var mailboxApp: MailboxApp
        private lateinit var username: String
        private lateinit var prefs: SharedPreferences
        private lateinit var model: PostViewModel

        fun getUsername(): String {
            return username
        }

        fun setUsername(newUsername: String) {
            username = newUsername
            // Store username for later, in sharedPrefs
            with (prefs.edit()) {
                putString(getInstance().applicationContext.getString(R.string.username_pref), username)
                apply()
            }
        }

        fun getPrefs(): SharedPreferences{
            return prefs
        }

        fun getInstance(): MailboxApp {
            return mailboxApp
        }

        fun getPostEntries(): MutableList<PostLogEntry> {
            //update Logview fragment
            return postLogEntryList
        }

        fun setPostEntries(updatedPostLogEntryList: MutableList<PostLogEntry>) {
            postLogEntryList = updatedPostLogEntryList
            model.postEntries.value = postLogEntryList
        }

        fun getUtil(): Util {
            return util
        }

        fun setPostViewModel(postViewModel: PostViewModel){
            model = postViewModel
        }
        fun getPostViewModel(): PostViewModel{
            return model
        }
    }
}