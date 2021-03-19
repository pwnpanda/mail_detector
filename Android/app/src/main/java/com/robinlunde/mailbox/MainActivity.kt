package com.robinlunde.mailbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get time variables for text from view
        val timestampTime = findViewById<TextView>(R.id.timestamp_time)
        val timestampDay = findViewById<TextView>(R.id.timestamp_day)
        // Get button from view
        val button = findViewById<Button>(R.id.Button)
        // Get values from Bluetooth
        // todo
        // If value received, show push notification!
        // if pressed, shows main view - updated
        // todo
        // Parse string from BT - Split into time and day
        // todo
        // Set values in variables for timestamps
        timestampTime.text = "hh:mm:ss"
        timestampDay.text = "12.12.2012"

        // Register activity for button
        // What happens when button is clicked
        button.setOnClickListener{
            // Send reply by BT
            // TODO
            val toasted = Toast.makeText(applicationContext, "Hello!", Toast.LENGTH_SHORT)
            // Show toast
            toasted.show()
        }
    }
}