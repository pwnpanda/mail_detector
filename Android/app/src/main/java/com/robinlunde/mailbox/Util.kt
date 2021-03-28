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
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

class Util internal constructor(context: Context){
    class LogItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)
    private val httpRequests = HttpRequestLib(context)

    init {
        // on init
        val myHandler = Handler(Looper.getMainLooper())
        myHandler.postDelayed(object: Runnable{
            override fun run() {
                val data = getLogs()
                MailboxApp.setPostEntries(data)
                //1 second * 60 * 30 = 30 min
                myHandler.postDelayed(this, 1000 * 60 * 30)
            }
            //1 second * 60 * 30 = 30 min
        }, 1000 * 60 * 30)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun tryRequest(type: String, timestamp: String?, id: Int?): Boolean {
        // Do async thread with network request
        var sent = false
        var tries = 1
        do {
            val thread = Thread {
                // Try to send web request
                try {
                    when(type) {
                        MailboxApp.getInstance().getString(R.string.sendLogsMethod) -> {
                            sendLog(timestamp!!).also { sent = it }
                        }
                        MailboxApp.getInstance().getString(R.string.deleteLogsMethod) -> {
                            delLog(id!!).also { sent = it }
                        }
                        else -> throw java.lang.Exception("Unknown http method!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Log.d("Thread", "Sleeping")
            // 5 seconds
            val base: Double = 5000.0
            // Exponentially increase wait time between tries
            val time: Double = base.pow(n = tries)
            Thread.sleep(time.toLong())
            Log.d("Thread", "Trying transmission $tries / 6")
            thread.start()
            tries++
            // Check for giving up
            if (tries >= 7 || sent) {
                Log.d("Thread", "Tried 6 transmissions but failed - Giving up! ")
                val toast = Toast.makeText(MailboxApp.getInstance().applicationContext, "Failed to save timestamp! Giving up!", Toast.LENGTH_LONG)
                toast.show()
                break
            }
        } while (!sent)

        if(sent) {
            MailboxApp.setPostEntries(getLogs())
        }

        return sent
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendLog(timestamp: String): Boolean {
        val res = runBlocking{
            // Create thread
            var tmpRes = false
            val thread = Thread {
                // Try to send webrequest
                try {
                    httpRequests.sendDataWeb(timestamp).also {
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
        Log.d("HTTP-Post", res.toString())
        return res
    }

    fun getLogs(): MutableList<PostLogEntry>{
        val res = runBlocking{
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
        val res = runBlocking{
            // Create thread
            var tmpRes = false
            val thread = Thread {
                // Try to send webrequest
                try {
                    tmpRes = true
                    //httpRequests.deleteLog(id).also {
                        //tmpRes = it
                    //}
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
            return@runBlocking tmpRes
        }
        Log.d("HTTP-Del", res.toString())
        return res
    }

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