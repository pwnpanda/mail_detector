package com.robinlunde.mailbox

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log

class MailboxApp : Application() {
    private lateinit var myNotificationManager: MyNotificationManager

    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        util = Util(applicationContext)
        prefs = this.getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE)
        username = prefs.getString(getString(R.string.username_pref), "").toString()
        util.startDataRenewer()
        myNotificationManager = MyNotificationManager()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.channel_id), name, importance).apply {
                description = descriptionText
            }
            myNotificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private lateinit var util: Util
        private lateinit var postLogEntryList: MutableList<PostLogEntry>
        private lateinit var mailboxApp: MailboxApp
        private lateinit var username: String
        private lateinit var prefs: SharedPreferences
        private lateinit var model: PostViewModel
        private lateinit var myNotificationManager: MyNotificationManager
        private lateinit var pushNotificationIds: MutableList<Int>


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

        fun getPushIds(): MutableList<Int>{
            return pushNotificationIds
        }

        fun pushNotification(timestamp: String){
            val pushId = myNotificationManager.createPush(timestamp)
            pushNotificationIds.add(pushId)
        }
    }
}