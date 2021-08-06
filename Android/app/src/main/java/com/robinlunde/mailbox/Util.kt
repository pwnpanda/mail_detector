package com.robinlunde.mailbox

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.robinlunde.mailbox.datamodel.MyMessage
import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.debug.ScanType
import com.robinlunde.mailbox.network.HttpRequestLib
import com.robinlunde.mailbox.triggers.RepeatedTrigger
import kotlinx.coroutines.*
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow

// TODO http data has become blocking when changing fragments!!

class Util {
    private lateinit var myNotificationManager: MyNotificationManager
    private lateinit var alarmPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager
    private val tag = "Util -"

    class LogItemViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    private val httpRequests = HttpRequestLib()
    private val updateURL: URL = URL(
        MailboxApp.getInstance().getString(R.string.update_url)
    )

    // ----------------------------- Notification -------------------------------


    fun pushNotification(message: MyMessage, pillAlert: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myNotificationManager.createPush(message, pillAlert)
        } else {
            Log.d("Push", "Android version too old, ignoring push notification!")
        }
    }

    // ----------------------------- HTTP -------------------------------

    /** TODO Refactor all http requests to be async
     * Seems better to be callback based!
     * THink about how to do it.
     * https://www.baeldung.com/guide-to-okhttp
     * https://stackoverflow.com/a/34967554
     */

    fun startDataRenewer() {
        //Log.e("Debug", "Util initiated")
        // on init
        val context = MailboxApp.getInstance()
        myNotificationManager = MyNotificationManager(context)
        val myHandler = Handler(Looper.getMainLooper())
        MailboxApp.getAppScope().launch {
            myHandler.postDelayed(object : Runnable {
                override fun run() {
                    // Check if error when getting new data
                    if (!getDataWeb(null)) {
                        Log.d("HTTP-Get", "Error when getting logs from server")
                    }
                    MailboxApp.getBTConn().bleScan(ScanType.BACKGROUND)
                    val lastCheck = getDataWeb(updateURL)
                    // if there was new mail
                    if (!lastCheck) {
                        Log.d("HTTP-Get", "Error when checking for new status with server")
                    }

                    //1 second * 60 * 30 = 30 min
                    // 1000 * 60 * 10 = 10min between updates in prod!
                    // Use 1000 * 10 for testing (10sec)
                    myHandler.postDelayed(this, (1000 * 60 * 10).toLong())
                }
                //1 second delay before first start
            }, 1000)
        }
    }

    suspend fun tryRequest(type: String, timestamp: String?, id: Int?, newMail: Boolean?): Boolean {
        return suspendCoroutine {
            val context = MailboxApp.getInstance()
            // Do async thread with network request
            Log.d("TryToRequest", "Type: $type Timestamp: $timestamp Id: $id")


            var sent = false
            var tries = 0
            do {
                // 5 seconds
                val base = 5000.0
                // Exponentially increase wait time between tries
                val time: Double = base.pow(n = tries)

                val thread = Thread {
                    // Try to send web request
                    try {
                        Log.d("Thread", "Sleeping")
                        Thread.sleep(time.toLong())
                        when (type) {
                            // Send the log off to api after picking it up
                            context.getString(R.string.sendLogsMethod) -> {
                                if (timestamp == null) {
                                    Log.d(
                                        "Error",
                                        "Timestamp is null when trying to add new post log entry"
                                    )
                                    throw NullPointerException()
                                }
                                sendLog(timestamp).also { sent = it }
                            }

                            // Delete an entry
                            context.getString(R.string.deleteLogsMethod) -> {
                                if (id == null) {
                                    Log.d("Error", "Id is null when trying to delete log entry")
                                    throw NullPointerException()
                                }
                                delLog(id).also { sent = it }
                            }

                            // Look for new post submitted by others (status)
                            context.getString(R.string.get_last_status_update_method) -> {
                                getDataWeb(updateURL).also { sent = it }
                            }

                            // New communication with BT device / new mail pickup - share with online api
                            context.getString(R.string.set_last_status_update_method) -> {
                                if (newMail == null) {
                                    Log.d(
                                        "Error",
                                        "newMail is null when trying to set new status update"
                                    )
                                    throw NullPointerException()
                                }
                                setLastUpdate(
                                    PostUpdateStatus(
                                        newMail,
                                        timestamp = getTime(),
                                        MailboxApp.getUsername()
                                    )
                                ).also { sent = it }
                            }

                            // Get update from server (logs)
                            context.getString(R.string.get_logs) -> {
                                getDataWeb(null).also { sent = it }
                            }

                            else -> throw java.lang.Exception("Unknown http method!")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Log.d("Thread", "Trying transmission $tries / 6")
                thread.start()
                thread.join()
                tries++

                // Check if we succeeded or if we are giving up
                if (tries >= 7 || sent) {
                    if (tries >= 7) {
                        Log.d("Thread", "Tried 6 transmissions but failed - Giving up! ")
                        Toast.makeText(
                            MailboxApp.getInstance().applicationContext,
                            "Failed to save timestamp! Giving up!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.d("Thread", "Transmission success for type: $type!")
                    }
                    break
                }
            } while (!sent)
        }
    }

    // Set last time we got info from BT Device
    fun setLastUpdate(data: PostUpdateStatus): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            try {
                httpRequests.setNewUpdateWeb(data).also {
                    tmpRes = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Log.d("HTTP-Post", res.toString())
        // Update with latest info from server
        if (!getDataWeb(updateURL)) Log.d("HTTP-Get", "Error when getting status from server!")

        return res
    }

    // Send log to server, telling it we picked up the mail
    private fun sendLog(timestamp: String): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            try {
                httpRequests.sendDataWeb(timestamp).also {
                    tmpRes = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Log.d("HTTP-Post", res.toString())
        // Update with latest info from server
        if (!getDataWeb(updateURL)) Log.d("HTTP-Get", "Error when getting status from server!")
        return res
    }

    // Get data from API, can be Logs or Update
    fun getDataWeb(myUrl: URL?): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = ""
            val thread = Thread {
                // Try to send web request to base url
                try {
                    httpRequests.getDataWeb(myUrl).also {
                        tmpRes = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
            return@runBlocking tmpRes
        }
        val gotValidResult = res != ""
        Log.d("HTTP-Get", "Valid result? $gotValidResult")


        val mapper = jacksonObjectMapper()

        // Getting Update-data since we specified an URL
        if (myUrl != null && gotValidResult) {
            // Convert data to ArrayList using jackson
            if (res.contains("\"timestamp\":\"\"")) {
                Log.d("StatusUpdate-data", "Data not yet initialized in server!")
            } else {
                val dataParsed: PostUpdateStatus = mapper.readValue(res)
                Log.d("ParsedData", dataParsed.toString())
                // Call to store value
                updateStatus(dataParsed)
            }
        } else if (gotValidResult) {
            // Getting Log-data since we didn't specify an URL
            // Convert data to ArrayList using jackson
            val dataParsed: MutableList<PostLogEntry> = mapper.readValue(res)
            Log.d("ParsedData", dataParsed.toString())
            // Call to store value
            updateLogs(dataParsed)
        }

        // return true if we received data
        return gotValidResult
    }

    // Delete a log from the list
    private fun delLog(id: Int): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            // Try to send web request
            try {
                httpRequests.deleteLog(id).also { tmpRes = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Log.d("HTTP-Del", res.toString())
        // Fetch new data from web
        if (!getDataWeb(null)) Log.d("HTTP-Get", "Error when getting new logs from server!")
        return res
    }

    // Helper function to update status from result of HTTP call
    private fun updateStatus(data: PostUpdateStatus) {
        MailboxApp.setStatus(data)
    }

    // Helper function to update logs from result of HTTP call
    private fun updateLogs(data: MutableList<PostLogEntry>) {
        MailboxApp.setPostEntries(data)
    }

    // ----------------------------- BT -------------------------------
    fun btEnabled() {
        Log.d("BlueTooth", "Proxied from Util")
        MailboxApp.getBTConn().btEnabledConfirmed()
    }

    // ----------------------------- DIV -------------------------------

    fun getMyDate(str: String): String {
        if (str == "") {
            Log.e("getTime", "In string is empty!")
            throw java.lang.NullPointerException("Input string is empty")
        }
        return str.split("T")[0]
    }

    fun getMyTime(str: String): String {
        if (str == "") {
            Log.e("getTime", "In string is empty!")
            throw java.lang.NullPointerException("Input string is empty")
        }
        return str.split("T")[1].subSequence(0, 8).toString()
    }

    fun logButtonPress(msg: String) {
        Log.d("MenuButtonPressed", msg)
    }

    fun getTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().toString()
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-ddTHHmmssZ", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())
            Log.d("Outdated Time", currentDateAndTime)
            currentDateAndTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    // Minute value is 5 minutes before the alarm should trigger. Real value is 5 minutes after
    fun activateAlarm(hourTime: Int, minuteTime: Int) {
        val thisTag = "$tag activateAlarm"
        val hour = if (hourTime == -1) 21 else hourTime
        val minute = if (minuteTime == -1) 0 else minuteTime

        Log.d(thisTag, "Received values: $hour:$minute")

        cancelAlarm()

        val context: Context? = MailboxApp.getContext()

        alarmManager =
            (context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)!!


        val alarmIntent = Intent(context, RepeatedTrigger::class.java)
            .putExtra("hour", hour)
            .putExtra("minute", minute)

        Log.d(
            thisTag,
            "Intent values: ${
                alarmIntent.getIntExtra(
                    "hour",
                    -1
                )
            }:${alarmIntent.getIntExtra("minute", -1)}"
        )
        alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Current time
        val timeNow = Calendar.getInstance().timeInMillis

        // When the actual alarm is supposed to go off
        val alertTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)

            Log.d(thisTag, "Cur: $timeNow - alertTime: ${this.timeInMillis}")
            // If it is passed the trueAlertTime, add a day
            if (timeNow >= this.timeInMillis) {
                Log.d(thisTag, "Time passed - increasing day by 1")
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            alertTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmPendingIntent
        )
    }


    private fun cancelAlarm() {
        try {
            // If the alarm has been set, cancel it.
            alarmManager.cancel(alarmPendingIntent)
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("$tag cancelAlarm", "This is fine - AlertManager has not yet been initialized!")
        } catch (e: java.lang.Exception) {
            Log.d("$tag cancelAlarm", e.stackTraceToString())
            Log.d("$tag cancelAlarm", "PendingIntent likely not set. WHat type of error??")
        }
    }
}