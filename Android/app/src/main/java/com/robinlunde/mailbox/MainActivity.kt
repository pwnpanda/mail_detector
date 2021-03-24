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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import kotlin.math.pow


class MainActivity() : AppCompatActivity() {

    // TODO
    // - Save name on first start
    // - Send data to API
        // Name - Timestamp of post received (from BT) - Timestamp of pickup
    // Get data from BT
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val httpRequests = HttpRequestLib(applicationContext)
        Util.init(httpRequests)
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
                //goto fragment, if username is set
                showStatus()
            }
            R.id.logs -> {
                //goto fragment, if username is set
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
                    httpReq?.sendDataWeb(timestamp).also {
                        if (it != null) {
                            sent = it
                        }
                    }
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

    private fun showLogs(): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)
        val res = runBlocking{
            // Create thread
            var res = ""
            val thread = Thread {
                // Try to send webrequest
                try {
                    httpReq?.getDataWeb().also {
                        if (it != null) {
                            res = it
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
            return@runBlocking res
        }
        Log.d("test", "res")
        if (res != "") {
            renderRecyclerView(res)
        } else {
            // Set error message in activity_log!
            findViewById<RecyclerView>(R.id.post_entries).visibility = View.INVISIBLE
            var error = findViewById<TextView>(R.id.error_logs)
            error.visibility = View.VISIBLE
        }
        return true
    }

    // Move to fragment?
    private fun renderRecyclerView(data: String): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)

        // data to populate the RecyclerView with
        // Convert data to ArrayList using jackson
        val mapper = jacksonObjectMapper()
        val dataParsed: List<PostEntry> = mapper.readValue(data)
        Log.e("Data", dataParsed.toString())

        val adapter = PostAdapter()
        binding.dataParsed.adapter = adapter
        // --------------------------------

        // set up the RecyclerView
        val postEntries = findViewById<RecyclerView>(R.id.post_entries)
        postEntries.layoutManager = LinearLayoutManager(this)




        //  Create adapter and click handler for recycler view                                       This gives position of clicked item
        postEntries.adapter = PostRecyclerViewAdapter(dataParsed, this){ position: Int ->
            Log.e("List clicked", "Clicked on item at position $position")
        }

        return true
    }
}