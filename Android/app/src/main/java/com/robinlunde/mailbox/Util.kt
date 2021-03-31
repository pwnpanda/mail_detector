package com.robinlunde.mailbox

import android.content.Context
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
import kotlinx.coroutines.*
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.O)
class Util(context: Context) {
    class LogItemViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    private val httpRequests = HttpRequestLib()
    private val myNotificationManager: MyNotificationManager = MyNotificationManager(context)
    private var pushNotificationIds: MutableList<Int> = mutableListOf()

    // ----------------------------- Notification -------------------------------

    fun getPushIds(): MutableList<Int> {
        return pushNotificationIds
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushNotification(timestamp: String) {
        val pushId = myNotificationManager.createPush(timestamp)
        pushNotificationIds.add(pushId)
        // TODO
        // Change to right fragment and add data!
    }

    // ----------------------------- HTTP -------------------------------

    fun startDataRenewer() {
        //Log.e("Debug", "Util initiated")
        // on init
        val myHandler = Handler(Looper.getMainLooper())
        var first = true
        MailboxApp.getAppScope().launch {
            myHandler.postDelayed (object : Runnable {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun run() {
                    val data = getLogs()
                    MailboxApp.setPostEntries(data)
                    // TODO Review this! This is not where it belongs :)
                    if (!first) {
                        Log.d("Push", "Push started!")
                        pushNotification("12.12.12")
                    }
                    //1 second * 60 * 30 = 30 min
                    first = false
                    // TODO Change this to 1000 * 60 * 30 for 30min between updates in prod!
                    myHandler.postDelayed(this, 1000 * 10 * 1)
                }
                //1 second delay before first start
            }, 1000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun tryRequest(type: String, timestamp: String?, id: Int?): Boolean {
        // Do async thread with network request
        Log.d("TryToRequest", "Type: $type Timestamp: $timestamp Id: $id")
        var sent: Boolean
        runBlocking {
            var tries = 0
            do {
                // 5 seconds
                val base = 5000.0
                // Exponentially increase wait time between tries
                val time: Double = base.pow(n = tries)
                Log.d("Coroutine", "Trying transmission $tries / 6")
                val deferred = doAsyncCoroutine(type, time, timestamp, id)
                tries++
                sent = deferred

                // Check for giving up
                if (tries >= 7 || sent) {
                    if (tries >= 7) {
                        Log.d("Thread", "Tried 6 transmissions but failed - Giving up! ")
                        val toast = Toast.makeText(
                            MailboxApp.getInstance().applicationContext,
                            "Failed to save timestamp! Giving up!",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    } else {
                        Log.d("Thread", "Transmission success for type: $type!")
                        val toast = Toast.makeText(
                            MailboxApp.getInstance().applicationContext,
                            "$type request has completed successfully!",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    }

                    break
                }
            } while (!sent)
        }

        if (sent) {
            MailboxApp.setPostEntries(getLogs())
        }

        return sent
    }

    private suspend fun doAsyncCoroutine(
        type: String,
        time: Double,
        timestamp: String?,
        id: Int?
    ): Boolean {
        return MailboxApp.getAppScope().async {
            try {
                Log.d("Coroutine", "Sleeping")
                delay(time.toLong())
                when (type) {
                    MailboxApp.getInstance().getString(R.string.sendLogsMethod) -> {
                        if (timestamp == null) {
                            Log.d(
                                "Error",
                                "Timestamp is null when trying to add new post log entry"
                            )
                            throw NullPointerException()
                        }
                        return@async sendLog(timestamp)
                    }
                    MailboxApp.getInstance().getString(R.string.deleteLogsMethod) -> {
                        if (id == null) {
                            Log.d("Error", "Id is null when trying to delete log entry")
                            throw NullPointerException()
                        }
                        return@async delLog(id)
                    }
                    else -> throw java.lang.Exception("Unknown http method!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.await() as Boolean
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendLog(timestamp: String): Boolean {
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
        return res
    }

    fun getLogs(): MutableList<PostLogEntry> {
        val res = runBlocking {
            // Create thread
            var tmpRes = ""
            val thread = Thread {
                // Try to send web request
                try {
                    httpRequests.getDataWeb().also {
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
        Log.d("HTTP-Get", res)

        // Convert data to ArrayList using jackson
        val mapper = jacksonObjectMapper()
        val dataParsed: MutableList<PostLogEntry> = mapper.readValue(res)
        Log.d("ParsedData", dataParsed.toString())

        // See PostRecyclerViewAdapter
        return dataParsed
    }

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
        return res
    }

    // ----------------------------- DIV -------------------------------

    fun getMyDate(str: String): String {
        return str.split("T")[0]
    }

    fun getMyTime(str: String): String {
        return str.split("T")[1].subSequence(0, 8).toString()
    }

    fun logButtonPress(msg: String) {
        Log.d("MenuButtonPressed", msg)
    }
}