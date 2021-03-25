package com.robinlunde.mailbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.robinlunde.mailbox.databinding.ActivityMainBinding

// TODO
// - Save name on first start
// - Send data to API
// Name - Timestamp of post received (from BT) - Timestamp of pickup
// Get data from BT
//     showAlert();

class MainActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // TODO init httprequests here
        
        //val toasted = Toast.makeText(applicationContext, "Hello!", Toast.LENGTH_SHORT)
        // Show toast
        //toasted.show()
    }

    // todo create and inflate menu here
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater

        inflater.inflate(R.menu.my_menubar, menu)
        // return true so that the menu pop up is opened
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logo -> {
                //Show status screen
                showStatus()
                // return true so that the menu pop up is opened
                true
            }
            R.id.logs -> {
                // Show log screen
                showLogs()
                // return true so that the menu pop up is opened
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogs() {
        Log.d("Logs", "ShowLogs in main")
    }

    private fun showStatus() {
        Log.d("Status", "ShowStatus in main")
    }
}