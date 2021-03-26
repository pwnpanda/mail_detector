package com.robinlunde.mailbox

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.robinlunde.mailbox.alert.AlertFragment
import com.robinlunde.mailbox.databinding.ActivityMainBinding
import com.robinlunde.mailbox.login.LoginFragment

// TODO
// - Save name on first start
// - Send data to API
// Name - Timestamp of post received (from BT) - Timestamp of pickup
// Get data from BT
//     showAlert();

class MainActivity() : AppCompatActivity(), OnFragmentInteractionListener  {
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        // TODO init httprequests here

        val myFragController = this.findNavController(R.id.fragment_holder)

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
                // https://developer.android.com/guide/fragments/transactions
                // startActivity()
               // val fg = AlertFragment.newInstance("temp", "test")
                
                // fragmentTransaction = fragmentManager.beginTransaction()
                // fragmentManager.commit { setReorderingAllowed(true) replace<AlertFragment>(R.id.) }
                // fragmentTransaction.addToBackStack()
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

    override fun changeFragment(id: Int){
        if (id == 1) {
            supportFragmentManager.beginTransaction().replace(R.id.frag_login, LoginFragment()).commit()
        } else if (id == 2) {
            supportFragmentManager.beginTransaction().replace(R.id.frag_alert, AlertFragment()).commit()
        }
    }
}