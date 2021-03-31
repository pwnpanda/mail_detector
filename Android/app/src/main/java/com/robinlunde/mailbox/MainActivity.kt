package com.robinlunde.mailbox

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.robinlunde.mailbox.databinding.ActivityMainBinding
import kotlinx.coroutines.cancel


// TODO
// Name - Timestamp of post received (from BT) - Timestamp of pickup
// Get data from BT then call
// MailboxApp.pushNotification(timeStamp)

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    // Create BT adapter
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // Check if we have location permissions
    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    // Supportive function for checking permissions
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    init {
        myActivity = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Requires BLE to run!", Toast.LENGTH_SHORT).show()
            finish()
        }
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

    // Handles clicks on the menu
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

    // Currently does nothing... ? TODO Check and fix
    override fun onNewIntent(intent: Intent?) {
        Log.d("Intent!", "Intent received")
        super.onNewIntent(intent)
    }

    // Make sure BT is enabled when we resume app
    // TODO this should be probably improved UI wise as well
    override fun onResume() {
        super.onResume()
        promptEnableBluetooth()
    }

    // Supportive function
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
            Toast.makeText(this,
                "This app requires bluetooth, please turn BlueTooth on!",
                Toast.LENGTH_LONG).show()
        } else{
            MailboxApp.getUtil().btEnabled()
        }
    }

    // If we get the wrong result, we call the function recursively, as app does not work without BT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }


    // Cleanup when activity is dead
    // TODO is this correct?
    override fun onDestroy() {
        super.onDestroy()
        MailboxApp.getAppScope().cancel()
    }
    companion object{
        lateinit var myActivity: MainActivity
        private const val ENABLE_BLUETOOTH_REQUEST_CODE = 13370110
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}