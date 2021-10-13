package com.robinlunde.mailbox

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.robinlunde.mailbox.databinding.ActivityMainBinding
import com.robinlunde.mailbox.debug.ScanType
import timber.log.Timber


// TODO
//  Get data from BT, call MailboxApp.setStatus(status), and send new status to web by calling

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private var permCheckCount = 0

    // Create BT adapter
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    init {
        myActivity = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        myActivity = this
        super.onCreate(savedInstanceState)

        // Setup bluetooth
        val PERMISSION_CODE = getString(R.string.bt_id_integer).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    myActivity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_CODE
                )
            }
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Requires BLE to run!", Toast.LENGTH_SHORT).show()
            finish()
        }
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        onNewIntent(intent)
    }

    // create and inflate menu here
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater

        inflater.inflate(R.menu.my_menubar, menu)

        /** Try to find out how to set size! Currently too big an icon
         * supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_HOME or
         * ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_USE_LOGO
         * supportActionBar!!.title = ""
         * supportActionBar!!.setIcon(R.mipmap.mailbox_border_appicon)
         */

        // return true so that the menu pop up is opened
        return true
    }

    // Handles clicks on the menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Always arrives here first
        return when (item.itemId) {
            R.id.alert -> {
                MailboxApp.setClickCounterZero()
                MailboxApp.getUtil().logButtonPress("Main - alarm")
                false
            }

            R.id.logs -> {
                MailboxApp.setClickCounterZero()
                MailboxApp.getUtil().logButtonPress("Main - logs")
                false
            }

            R.id.bluetooth -> {
                MailboxApp.getUtil().logButtonPress("Main - BT")
                // MailboxApp.getBTConn().startScan()
                val cnt = MailboxApp.incrementClickCounter()
                if (cnt == 1) {
                    MailboxApp.getBTConn().bleScan(ScanType.ACTIVE)
                    // Sets clickCounter to 0 in 3 seconds
                    Handler(Looper.myLooper()!!).postDelayed({
                        MailboxApp.setClickCounterZero()
                    }, 3000)
                }
                false
            }

            R.id.pill -> {
                MailboxApp.getUtil().logButtonPress("Main - pill")
                MailboxApp.setClickCounterZero()
                false
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Currently just logs information for debugging
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("Intent received: $intent")

        // It is my push notification intent!
        if (intent.getBooleanExtra(getString(R.string.app_name), false)) {
            Timber.d("My intent received: $intent")
            // No need to update fragment, as it does so automatically upon intent clicked ^~^
        }
        if (intent.getBooleanExtra("pill", false)) {
            val curFragment: Fragment? = supportFragmentManager.findFragmentByTag("fragment_login")
            /** TODO If you want pill notification to move to pill fragment! Add boolean to intent and iterate over fragments as follows
             *  val visible = curFragment!!.isVisible
             * val curController = NavHostFragment.findNavController(curFragment)
             *
             * switch (fragment_tags)
             *   login -> curController.navigate(LoginFragmentDirections.actionLoginFragmentToPillFragment())
             */
        }
    }

    // Make sure BT is enabled when we resume app
    override fun onResume() {
        super.onResume()
        if (promptGivePermission()) MailboxApp.getUtil().btEnabled()
    }

    // ------------------- BT ---------------------

    // Supportive function
    private fun promptGivePermission(): Boolean {
        if (permCheckCount > 0) {
            Toast.makeText(
                this,
                "This app requires these permissions! Please start app again and give permissions!",
                Toast.LENGTH_LONG
            ).show()
            finishAndRemoveTask()
        }
        when {
            // If we do not have BT permission
            !bluetoothAdapter.isEnabled -> {
                enableBluetooth()
            }

            // If we do not have location permissions
            !hasLocationPermission -> {
                /*Toast.makeText(this,
                    "This app requires location permission for Bluetooth Low Energy, please allow it!",
                    Toast.LENGTH_LONG).show()*/
                requestLocationPermission()
            }

            // All permissions ready, run BT scan
            else -> return true
        }

        // Never reached
        return false
    }

    // Send BT enable intent to OS
    private fun Activity.enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, RequestCodeConst.EnableBluetooth)
    }

    // If we get the wrong result, we call the function recursively, as app does not work without BT or Location
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // If we requested BT
            RequestCodeConst.EnableBluetooth -> {
                Timber.d("Bluetooth permission not granted")
                if (resultCode != RESULT_OK) {
                    promptGivePermission()
                }
            }
        }
    }

    // Catch result of permission check
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.d("Permission counter is: $permCheckCount")

        when (requestCode) {

            RequestCodeConst.BothPermissions -> {
                // Permissions granted
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Timber.d("Permission for BT and Loc granted")
                } else {
                    // be unhappy
                    Timber.d("Permission for BT and Loc DENIED!")
                    permCheckCount++
                }
            }

            RequestCodeConst.LocationPermission -> {
                // Permissions granted
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Be happy
                    Timber.d("Permission for Loc granted")

                } else {
                    // be unhappy
                    Timber.d("Permission for Loc DENIED!")
                    permCheckCount++
                }
            }
            else -> {
                Timber.d("Unknown permission result")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Constants for requesting permissions
    private object RequestCodeConst {
        const val BothPermissions = 55000
        const val EnableBluetooth = 55001
        const val LocationPermission = 55002
    }

    /**
     * Shows the native Android permission request dialog.
     *
     * The result of the dialog will come back via [Activity.onRequestPermissionsResult] method.
     */
    private fun Activity.requestLocationPermission() {
        val permissions = arrayOf(ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, RequestCodeConst.LocationPermission)
    }

    // Check if we have location permissions
    private val Context.hasLocationPermission: Boolean
        get() = hasPermission(ACCESS_FINE_LOCATION)

    // Supportive function for checking permissions
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    companion object {
        lateinit var myActivity: MainActivity
    }
}