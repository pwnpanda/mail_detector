package com.robinlunde.mailbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

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
        //val timestamp = "21:12:00 12.12.2012"
        val timestamp = ""
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
        }
        return true;
    }

    private fun showLogs(): Boolean {
        // Show a different view!
        setContentView(R.layout.activity_log)
        return true;
    }
}