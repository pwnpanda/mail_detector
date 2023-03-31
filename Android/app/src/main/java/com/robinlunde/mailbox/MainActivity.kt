package com.robinlunde.mailbox

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.robinlunde.mailbox.databinding.ActivityMainBinding
import timber.log.Timber


// TODO
//  Get data from BT, call MailboxApp.setStatus(status), and send new status to web by calling

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private var permCheckCount = 0

    @RequiresApi(Build.VERSION_CODES.S)
    private val PERMISSIONS_LOCATION = arrayOf(
        ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )
    @RequiresApi(Build.VERSION_CODES.S)
    private val PERMISSIONS_NOTIFICATION = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    // Create BT adapter
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    init {
        myActivity = this
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        myActivity = this
        super.onCreate(savedInstanceState)

        promptGivePermission2()

        // Setup bluetooth
        val PERMISSION_CODE = getString(R.string.bt_id_integer).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    myActivity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.BLUETOOTH_CONNECT),
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
    @RequiresApi(Build.VERSION_CODES.S)
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
                    MailboxApp.getBTConn().bleConnect()
                    //MailboxApp.getBTConn().bleScan(ScanType.ACTIVE)
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        if (promptGivePermission2()) MailboxApp.getUtil().btEnabled()
    }

    // ------------------- BT ---------------------
    // https://stackoverflow.com/questions/70245463/java-lang-securityexception-need-android-permission-bluetooth-connect-permissio

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun promptGivePermission2(): Boolean {
        val permissionBTLoc =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        if (permissionBTLoc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_LOCATION,
                1
            )
        }

        val permissionNotification =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionNotification != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_NOTIFICATION,
                2
            )
        }



        return true
    }

    companion object {
        lateinit var myActivity: MainActivity
    }
}