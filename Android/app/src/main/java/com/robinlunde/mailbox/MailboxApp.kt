package com.robinlunde.mailbox

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.robinlunde.mailbox.alert.AlertViewModel
import com.robinlunde.mailbox.datamodel.MyMessage
import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.debug.DebugViewModel
import com.robinlunde.mailbox.logview.PostViewModel
import com.robinlunde.mailbox.network.NativeBluetooth
import fr.bipi.tressence.file.FileLoggerTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class MailboxApp : Application() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        mailboxApp = this

        // Log properly
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return String.format(
                    "Class:%s: Line: %s, Method: %s",
                    super.createStackElementTag(element),
                    element.lineNumber,
                    element.methodName
                )
            }
        })
        try {
            val logsDir = File(filesDir, "logs")
            if (!logsDir.exists()) logsDir.mkdirs()
            val t: Timber.Tree =
                FileLoggerTree.Builder()
                    .withFileName("MailboxApp%g.log")
                    .withDir(logsDir)
                    .withSizeLimit(75000)
                    .withFileLimit(5)
                    .withMinPriority(Log.DEBUG)
                    .appendToFile(true)
                    .build()

            Timber.plant(t)
        } catch (e: IOException) {
            Timber.w(e.printStackTrace().toString())
        }

        util = Util()

        // Create scope and start handler in coroutine
        appScope = MainScope()

        appScope.launch {
            val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            prefs = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                mailboxApp,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            username = prefs.getString(getString(R.string.username_pref), "").toString()


            //Create alarm
            util.activateAlarm(
                prefs.getInt(
                    "alarm_hour",
                    -1
                ),
                prefs.getInt(
                    "alarm_minute",
                    -1
                )
            )
        }

// Need to start with string long enough to not trigger fault
        status = PostUpdateStatus(
            false,
            "FOOTBARBAZFOOBARBAZ",
            getString(R.string.no_status_yet_username)
        )

// BT
        btConnection = NativeBluetooth()

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

        // BT
        private lateinit var btConnection: NativeBluetooth
        private var clickCounter = AtomicInteger()
        private var sensorData = mutableListOf<Double>()
        private lateinit var debugViewModel: DebugViewModel

        // Initialize to signal no data available
        private lateinit var status: PostUpdateStatus

        // Get context
        fun getContext(): Context? {
            return mailboxApp.applicationContext
        }

        // Get username
        fun getUsername(): String {
            return if (::username.isInitialized) username
            else ""
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

        // Get shared preferences
        fun getPrefs(): SharedPreferences {
            return prefs
        }

        // Get mutable data only, for instantiating LiveData
        fun getPostEntries(): MutableList<PostLogEntry> {
            return if (::postLogEntryList.isInitialized) postLogEntryList
            else mutableListOf()
        }

        // Update data in view, by updating postEntries object
        fun setPostEntries(updatedPostLogEntryList: MutableList<PostLogEntry>) {
            postLogEntryList = updatedPostLogEntryList
            try {
                postViewModel.mutablePostEntries.postValue(postLogEntryList)
            } catch (e: UninitializedPropertyAccessException) {
                Timber.d("Model not yet instantiated - Could not update data for view")
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

        fun setAlertModel(myModel: AlertViewModel) {
            alertViewModel = myModel
        }

        // Get application scope
        fun getAppScope(): CoroutineScope {
            return appScope
        }

        // Get Connector for Bluetooth operations
        fun getBTConn(): NativeBluetooth {
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
         * newMail False -> False = Update timestamp and person who did last check
         */
        fun setStatus(newStatus: PostUpdateStatus) {
            // Check if current and previous status is the same
            val isEqual = status.newMail == newStatus.newMail
            // If value after update is the same as before
            if (isEqual) {
                Timber.d("Same value as before, no push needed")
                when (newStatus.newMail) {
                    true -> Timber.d("No new information. Do nothing!")
                    false -> Timber.d("Maybe new timestamp for last check! Arbitrary update")
                }
                // If value after update is different
            } else {
                val msg: MyMessage = when (newStatus.newMail) {
                    true -> {
                        Timber.d("New mail detected! Send push and update fragment! Data: $newStatus")
                        // This is return value
                        MyMessage(
                            "New mail detected!",
                            "The mail was delivered at ${newStatus.time} on ${
                                newStatus.date
                            }!"
                        )
                    }
                    false -> {
                        Timber.d("Mail picked up! Send push and update fragment")
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
                if (status.username != getInstance().getString(R.string.no_status_yet_username)) {
                    Timber.d("Not first run, Send push notification!")
                    util.pushNotification(msg)
                }
            }

            // Update value for checking next time :)
            // This needs to happen before updating fragment, as the fragment relies on getStatus()
            status = newStatus
            try {
                alertViewModel.currentStatus.postValue(newStatus)
            } catch (e: UninitializedPropertyAccessException) {
                Timber.d("Model not yet instantiated - Could not update data for view")
            } catch (e: Exception) {
                throw e
            }
        }

        // function for handling new data from BT
        fun newBTData(time: String, onlyTimestamp: Boolean) {

            // TODO
            //  ensure correct format
            //  "timestamp":"2021-04-03T23:26:55.108"
            Timber.d("New data from device received: $time, only update the timestamp? $onlyTimestamp")

            // If we only update the current check timestamp and know nothing of the status of mail
            if (onlyTimestamp) {
                getUtil().setLastUpdate(
                    PostUpdateStatus(
                        getStatus().newMail,
                        time,
                        getUsername()
                    )
                )
                return
            }

            /** TODO Calculate timestamp based on difference from current time
             * given value (time) is the time (in seconds) since the mail was detected.
             * We can calculate when it was, by removing valFromSensor seconds from the current timestamp
             * val sensorDetected = now() - valFromSensor
             * val pickupTime = now()
             */
            val postTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now().minusSeconds(time.toLong()).toString()
            } else {
                Timber.e("Android version too low! Please upgrade your os")
                throw error("Android version too low!")
            }

            Timber.d("Time is set to: $postTime, which is $time ago.")

            // add update to API server - this automatically updates local state as well
            getUtil().setLastUpdate(
                PostUpdateStatus(
                    newMail = true,
                    postTime,
                    getUsername()
                )
            )
        }

        // increment clickCounter
        fun incrementClickCounter(): Int {
            Timber.d("Increment counter +1! Value before increment: " + clickCounter.get())
            return clickCounter.incrementAndGet()
        }

        // set clickCounter 0
        fun setClickCounterZero() {
            Timber.d("Set to 0! Current value of counter: " + clickCounter.get() + ".")
            clickCounter.set(0)
        }

        // get clickCounter
        fun getClickCounter(): Int {
            Timber.d("Value of counter: " + clickCounter.get())
            return clickCounter.get()
        }

        // Set debugViewModel
        fun setDebugViewModel(model: DebugViewModel) {
            debugViewModel = model
        }

        // Set rssi data in debug mode
        fun setRSSIData(data: Int) {
            Timber.d("New RSSI received: $data")
            try {
                debugViewModel.rssi.postValue(data)
            } catch (e: UninitializedPropertyAccessException) {
                Timber.d("SOFT ERROR - Model not yet instantiated - Could not update data for view")
            } catch (e: Exception) {
                throw e
            }
        }

        // Call from BT function!
        fun setSensorData(data: Double) {
            Timber.d("New SensorData received: $data")
            sensorData.add(data)
            val curValue = sensorData
            try {
                debugViewModel.sensorData.postValue(curValue)
            } catch (e: UninitializedPropertyAccessException) {
                Timber.d("Model not yet instantiated - Could not update data for view")
            } catch (e: Exception) {
                throw e
            }
        }
    }
}