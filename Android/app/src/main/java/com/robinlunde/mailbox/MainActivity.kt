package com.robinlunde.mailbox

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
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
            val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)


            // TODO init httprequests here
            //val navController = this.findNavController(R.id.navigation)
            //NavigationUI.setupActionBarWithNavController(this, navController, linearLayout)
            //NavigationUI.setupWithNavController(binding.navView, navController)

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
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.navigation)) || super.onOptionsItemSelected(item)
        /*when (item.itemId) {
            R.id.logo -> {
                //goto fragment, if username is set
                //showStatus()

            }
            R.id.logs -> {
                //goto fragment, if username is set
                //showLogs()
            }
        }
        return super.onOptionsItemSelected(item)
        */
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navigation)
        return NavigationUI.navigateUp(navController, linearLayout)
    }*/
}