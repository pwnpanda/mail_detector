package com.robinlunde.mailbox

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MailboxApp : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        util = Util(this@MailboxApp)
        prefs = this@MailboxApp.getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE)
        username = prefs.getString(getString(R.string.username_pref), "").toString()

        // Create scope and start handler in coroutine
        appScope = MainScope()
        appScope.launch {
            util.startDataRenewer()
        }

    }

    companion object {
        private lateinit var util: Util
        private lateinit var postLogEntryList: MutableList<PostLogEntry>
        private lateinit var mailboxApp: MailboxApp
        private lateinit var username: String
        private lateinit var prefs: SharedPreferences
        private lateinit var model: PostViewModel
        private lateinit var appScope: CoroutineScope

        fun getUsername(): String {
            return username
        }

        fun setUsername(newUsername: String) {
            username = newUsername
            // Store username for later, in sharedPrefs
            with(prefs.edit()) {
                putString(
                    getInstance().applicationContext.getString(R.string.username_pref),
                    username
                )
                apply()
            }
        }

        fun getInstance(): MailboxApp {
            return mailboxApp
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getPostEntries(): MutableList<PostLogEntry> {
            //update mutable data ONLY!
            postLogEntryList = util.getLogs()
            return postLogEntryList
        }

        // Update data in view
        fun setPostEntries(updatedPostLogEntryList: MutableList<PostLogEntry>) {
            postLogEntryList = updatedPostLogEntryList
            try {
                model.setPostEntries(postLogEntryList)
            }catch (e: UninitializedPropertyAccessException){
                Log.d("Soft error", "Model not yet instantiated - Could not update data for view")
            } catch (e: Exception){
                throw e
            }
        }

        fun getUtil(): Util {
            return util
        }

        fun setModel(myModel: PostViewModel) {
            model = myModel
        }

        fun getAppScope(): CoroutineScope {
            return appScope
        }
    }
}