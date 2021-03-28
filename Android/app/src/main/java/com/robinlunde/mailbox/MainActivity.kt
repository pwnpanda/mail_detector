package com.robinlunde.mailbox

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.robinlunde.mailbox.databinding.ActivityMainBinding

// TODO
// Name - Timestamp of post received (from BT) - Timestamp of pickup
// Get data from BT

class MainActivity() : AppCompatActivity()  {
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
    }

    // create and inflate menu here
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater

        inflater.inflate(R.menu.my_menubar, menu)
        // return true so that the menu pop up is opened
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // if it arrives here, something went wrong
        return when (item.itemId) {
            R.id.logo -> {
                MailboxApp.getUtil().logButtonPress("Main - logo")
                false
            }
            R.id.logs -> {
                MailboxApp.getUtil().logButtonPress("Main - logs")
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}