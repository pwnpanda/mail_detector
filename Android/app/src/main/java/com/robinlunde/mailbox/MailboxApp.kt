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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MailboxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        mailboxApp = this
        util = Util(this@MailboxApp)
        prefs = this@MailboxApp.getSharedPreferences(getString(R.string.username_pref), Context.MODE_PRIVATE)
        username = prefs.getString(getString(R.string.username_pref), "").toString()

        // Create scope and start handler in coroutine
        appScope = MainScope()
        btConnection = BlueToothLib()
        appScope.launch {
            // Setup refresher on API to update data every 30min
            util.startDataRenewer()

            // Setup bluetooth
            val PERMISSION_CODE = getString(R.string.bt_id_integer).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(baseContext,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.myActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        PERMISSION_CODE)
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
        private lateinit var model: PostViewModel
        private lateinit var appScope: CoroutineScope
        private lateinit var btConnection: BlueToothLib

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

        fun getAppScope(): CoroutineScope {
            return appScope
        }
        fun getBTConn(): BlueToothLib {
            return btConnection
        }
    }
}