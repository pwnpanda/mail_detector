package com.robinlunde.mailbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.robinlunde.mailbox.alert.AlertFragmentDirections
import com.robinlunde.mailbox.databinding.ActivityMainBinding
import com.robinlunde.mailbox.login.LoginFragmentDirections

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


        //val toasted = Toast.makeText(applicationContext, "Hello!", Toast.LENGTH_SHORT)
        // Show toast
        //toasted.show()
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
        return when (item.itemId) {
            R.id.logo -> {
                // get Username
                val username = MailboxApp.getUsername()
                /*
                TODO need to know which frag we are in...
                val frags = supportFragmentManager.fragments
                for (frag in frags){
                    Log.d("Fragment: ", "$frag. and ID: ${getString(frag.id} and View: ${frag.view}")
                }*/
                // If in login fragment
                this.findNavController(R.id.frag_login).navigate(LoginFragmentDirections.actionLoginFragmentToAlertFragment(username.toString()))
                // If in logview fragment
                //this.findNavController(R.id.frag_logview).navigate(LogviewFragmentDirections.actionLogviewFragmentToAlertFragment(username.toString()))
                logButtonPress("logo")
                true
            }
            R.id.logs -> {
                // Show log screen
                // if in AlertFragment
                this.findNavController(R.id.frag_alert).navigate(AlertFragmentDirections.actionAlertFragmentToLogviewFragment())
                // Else if in login fragment
                //this.findNavController(R.id.frag_login).navigate(LoginFragmentDirections.actionLoginFragmentToLogviewFragment())
                logButtonPress("logs")
                // return true so that the menu pop up is opened
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logButtonPress(msg: String) {
        Log.d("MenuButtonPressed", msg)
        //findNavController().navigate(R.id.)
    }
}