package com.robinlunde.mailbox

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.robinlunde.mailbox.alert.AlertViewModel
import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.logview.PostViewModel
import com.robinlunde.mailbox.network.BlueToothLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MailboxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        util = Util()
        prefs = this@MailboxApp.getSharedPreferences(
            getString(R.string.username_pref),
            Context.MODE_PRIVATE
        )
        username = prefs.getString(getString(R.string.username_pref), "").toString()
        // Need to start with string long enough to not trigger fault
        status = PostUpdateStatus(false, "FOOTBARBAZFOOBARBAZ", getString(R.string.no_status_yet_username))
        // Create scope and start handler in coroutine
        appScope = MainScope()
        btConnection = BlueToothLib()
        appScope.launch {
            // Setup refresher on API to update data every 30min
            util.startDataRenewer()

            // Setup bluetooth
            val PERMISSION_CODE = getString(R.string.bt_id_integer).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        baseContext,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        MainActivity.myActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        PERMISSION_CODE
                    )
                }
            }
        }

    }

    companion object {
        private lateinit var util: Util
        private lateinit var postLogEntryList: MutableList<PostLogEntry>
        private lateinit var mailboxApp: MailboxApp
        private lateinit var username: String
        private lateinit var prefs: SharedPreferences
        private lateinit var postViewModel: PostViewModel
        private lateinit var alertViewModel: AlertViewModel
        private lateinit var appScope: CoroutineScope
        private lateinit var btConnection: BlueToothLib
        // Initialize to signal no data available
        private lateinit var status: PostUpdateStatus

        data class MyMessage(val title: String, val text: String)

        // Get username
        fun getUsername(): String {
            return username
        }

        // Set username
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

        // Get application instance
        fun getInstance(): MailboxApp {
            return mailboxApp
        }

        // Get mutable data only, for instantiating LiveData
        fun getPostEntries(): MutableList<PostLogEntry> {
            return postLogEntryList
        }

        // Update data in view, by updating postEntries object
        fun setPostEntries(updatedPostLogEntryList: MutableList<PostLogEntry>) {
            postLogEntryList = updatedPostLogEntryList
            try {
                postViewModel.mutablePostEntries.postValue(postLogEntryList)
            } catch (e: UninitializedPropertyAccessException) {
                Log.d("Soft error", "Model not yet instantiated - Could not update data for view")
            } catch (e: Exception) {
                throw e
            }
        }

        // Get utility instance
        fun getUtil(): Util {
            return util
        }

        // Set PostViewModel
        fun setPostModel(myModel: PostViewModel) {
            postViewModel = myModel
        }

        fun setAlertModel(myModel: AlertViewModel){
            alertViewModel = myModel
        }

        // Get application scope
        fun getAppScope(): CoroutineScope {
            return appScope
        }

        // Get Connector for Bluetooth operations
        fun getBTConn(): BlueToothLib {
            return btConnection
        }

        // Get latest status of post, only for initiating LiveData
        fun getStatus(): PostUpdateStatus {
            return status
        }

        // Set latest status of post
        /**
         * newMail True -> True = do nothing
         * newMail True -> False = remove notification view & push that X picked up the mail
         * newMail: False -> True = Notification and details update
         * newMail False -> False, Update timestamp and person who did last check
         */
        fun setStatus(newStatus: PostUpdateStatus) {
            // Check if current and previous status is the same
            val isEqual = status.newMail == newStatus.newMail
            // If value after update is the same as before
            if (isEqual) {
                Log.d("Status", "Same value as before, no push needed")
                when (newStatus.newMail) {
                    true -> Log.d("Status", "No new information. Do nothing!")
                    false -> Log.d("Status", "Maybe new timestamp for last check! Arbitrary update")
                }
                // If value after update is different
            } else {
                val msg: MyMessage = when (newStatus.newMail) {
                    true -> {
                        Log.d("Status", "New mail detected! Send push and update fragment! Data: $newStatus")
                        // This is return value
                        MyMessage(
                            "New mail detected!",
                            "The mail was delivered at ${newStatus.time} on ${
                                newStatus.date
                            }!"
                        )
                    }
                    false -> {
                        Log.d("Status", "Mail picked up! Send push and update fragment")
                        // This is return value
                        MyMessage(
                            "Mail picked up by ${newStatus.username}!",
                            "Mail picked up at ${newStatus.time} on ${
                                newStatus.date
                            }! Say thanks!"
                        )
                    }
                }
                // Push Notification only if it is not the first run
                if (status.username != getInstance().getString(R.string.no_status_yet_username) ){
                    Log.d("Status", "Not first run, Send push notification!")
                    util.pushNotification(msg)
                }
            }

            // Update value for checking next time :)
            // This needs to happen before updating fragment, as the fragment relies on getStatus()
            status = newStatus
            try{
                alertViewModel.currentStatus.postValue(newStatus)
            } catch (e: UninitializedPropertyAccessException) {
                Log.d("Soft error", "Model not yet instantiated - Could not update data for view")
            } catch (e: Exception) {
                throw e
            }

            // Update fragment_alert - Do for all instances, doesn't hurt!
            // This happens automatically when notification is clicked.
            //MainActivity.myActivity.updateAlertFragment()
        }

    }
}