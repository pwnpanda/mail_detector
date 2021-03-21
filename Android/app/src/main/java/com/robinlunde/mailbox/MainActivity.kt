package com.robinlunde.mailbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Thread.sleep
import java.net.URL
import kotlin.math.pow


class MainActivity : AppCompatActivity() {

    // TODO
    // - Save name on first start
    // - Send data to API
        // Name - Timestamp of post received (from BT) - Timestamp of pickup
    // Get data from BT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val toasted = Toast.makeText(applicationContext, "Hello!", Toast.LENGTH_SHORT)
        // Show toast
        //toasted.show()

        showStatus();
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.my_menubar, menu)
        // return true so that the menu pop up is opened
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logo -> {
                showStatus()
            }
            R.id.logs -> {
                showLogs()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showStatus(): Boolean {
        setContentView(R.layout.activity_main)
        // Get time variables for text from view
        val timestampText = findViewById<TextView>(R.id.timestamp_text)
        val timestampTime = findViewById<TextView>(R.id.timestamp_time)
        val timestampDay = findViewById<TextView>(R.id.timestamp_day)
        // Get button from view
        val button = findViewById<Button>(R.id.button)
        // Get icon from view
        val icon = findViewById<ImageView>(R.id.post_box)
        // Get values from Bluetooth
        // todo
        // val timestamp = getValFromBT()

        // If value received, show push notification!
        // if pressed, shows main view - updated
        // todo
        // Parse string from BT - Split into time and day
        // todo
        val timestamp = "21:12:00 12.12.2012"
        //val timestamp = ""
        if (timestamp != ""){
            var timestampParts = timestamp.split(" ")
            Log.d("test", timestampParts.toString())
            timestampDay.text = timestampParts[1]
            timestampTime.text = timestampParts[0]

        } else {
            timestampText.text = "No new post received!"
            timestampDay.visibility = View.INVISIBLE
            timestampTime.visibility = View.INVISIBLE
            button.visibility = View.INVISIBLE
            icon.visibility = View.VISIBLE
        }

        // Register activity for button
        // What happens when button is clicked
        button.setOnClickListener{
            // Send reply by BT
            // TODO
            showLogs()
            // Try to send web request
            trySendDataWeb(timestamp)
        }
        return true;
    }

    private fun trySendDataWeb(timestamp: String) {
        // Do async thread with network request
        var sent: Boolean = false
        var tries: Int = 1
        do {
            // Create thread
            val thread = Thread {
                // Try to send webrequest
                try {
                    sendDataWeb(timestamp).also { sent = it }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Log.d("Thread", "Sleeping")
            // 5 seconds
            var base: Double = 5000.0
            // Exponentially increase wait time between tries
            var time: Double = base.pow(tries)
            // Sleep
            sleep(time.toLong())
            // Log try
            Log.d("Thread", "Trying transmission $tries / 6")
            // Start above thread
            thread.start()
            // Increase try counter
            tries++
            // Check for giving up
            if (tries >= 7) {
                sent = true
                Log.d("Thread", "Tried 6 transmissions but failed - Giving up! ")
                val toast = Toast.makeText(applicationContext, "Failed to save timestamp! Giving up!", Toast.LENGTH_LONG)
                // Show toast
                toast.show()
            }
        } while (!sent)
    }

    private fun sendDataWeb(timestamp: String): Boolean {
        val client = OkHttpClient()
        val url = URL("https://robinlunde.com/testme")

        //or using jackson
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("timestamp", timestamp)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
            //.addHeader("Authorization", "Bearer $token") TODO add auth
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        // Log.d("HTTP", "Response code: ${response.code}")
        //Response
        Log.d("HTTP", "Response Body: $responseBody")
        if (response.code == 200) {
            val toast = Toast.makeText(applicationContext, "Timestamp saved!", Toast.LENGTH_LONG)
            // Show toast
            toast.show()
        }

        return response.code == 200

    }

    private fun showLogs(): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)
        return true;
    }
}