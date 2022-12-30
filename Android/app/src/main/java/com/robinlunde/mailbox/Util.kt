package com.robinlunde.mailbox

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Build.VERSION_CODES.R
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.robinlunde.mailbox.alert.AlertFragmentDirections
import com.robinlunde.mailbox.datamodel.MyMessage
import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.User
import com.robinlunde.mailbox.debug.DebugFragmentDirections
import com.robinlunde.mailbox.debug.ScanType
import com.robinlunde.mailbox.logview.PostViewFragmentDirections
import com.robinlunde.mailbox.network.*
import com.robinlunde.mailbox.pills.*
import com.robinlunde.mailbox.repository.DayRepository
import com.robinlunde.mailbox.repository.PillRepository
import com.robinlunde.mailbox.repository.RecordRepository
import com.robinlunde.mailbox.triggers.RepeatedTrigger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// TODO http data has become blocking when changing fragments!!

class Util {
    private lateinit var myNotificationManager: MyNotificationManager
    private lateinit var alarmPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    class LogItemViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    class RecordItemViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    class PillItemViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    private val httpRequests = HttpRequestLib()

    private var httpRequestLib2 = HttpRequestLib2.getClient(this)

    private var apiInterfaceUser: ApiInterfaceUser =
        httpRequestLib2.create(ApiInterfaceUser::class.java)
    lateinit var authInterceptor: AuthenticationInterceptor

    private val updateURL: URL = URL(
        MailboxApp.getInstance().getString(R.string.post_update_url)
    )

    var user: User? = null
    val dayrepo: DayRepository = DayRepository(this)
    val pillrepo: PillRepository = PillRepository(this)
    val recordrepo: RecordRepository = RecordRepository(this)

    lateinit var pillUpdateAdapter: PillUpdateAdapter
    lateinit var pillLogAdapter: PillLogAdapter

    private var apiInterfaceMailNotifications: ApiInterfaceMailNotifications =
        httpRequestLib2.create(ApiInterfaceMailNotifications::class.java)

    // ----------------------------- Notification -------------------------------


    fun pushNotification(message: MyMessage, pillAlert: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (::myNotificationManager.isInitialized) myNotificationManager.createPush(
                message,
                pillAlert
            )
            else MyNotificationManager(MailboxApp.getContext()!!).createPush(message, pillAlert)
        } else {
            Timber.d("Android version too old, ignoring push notification!")
        }
    }

    // ----------------------------- HTTP -------------------------------

    /** TODO Refactor all http requests to be async
     * Seems better to be callback based!
     * THink about how to do it.
     * https://www.baeldung.com/guide-to-okhttp
     * https://stackoverflow.com/a/34967554
     */

    // TODO Rewrite all of the re-newers etc. below with coroutines and use updated network calls
    // TODO Then change login flow and data flow to fix "slow network" login issues

    private data class MailboxStatus(
        val curStatus: PostUpdateStatus,
        val logs: MutableList<PostLogEntry>
    )

    // Helper-flow for running task periodically
    // https://kotlinlang.org/docs/flow.html#flow-cancellation-basics
    // https://stackoverflow.com/questions/54827455/how-to-implement-timer-with-kotlin-coroutines
    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            MailboxApp.getBTConn().bleScan(ScanType.BACKGROUND)
            val getLogs =
                apiInterfaceMailNotifications.getRecentMailboxStatus() // getAllPostNotificationFromWebHelper(null)
            val getStatus =
                apiInterfaceMailNotifications.getLastMailboxStatus() //getAllPostNotificationFromWebHelper(updateURL)
            val myMailboxStatus = MailboxStatus(getStatus, getLogs)
            emit(myMailboxStatus)
            delay(period)
        }
    }

    // TODO Change name - remove 2
    fun startDataRenewer() {
        val appScope = MailboxApp.getAppScope()
        appScope.launch {
            // Run every 10 minutes, and wait 1 second before starting the first time
            tickerFlow((60 * 10).seconds, 1.seconds).collect { result ->
                newDataReceivedHelper(result)
            }
        }
    }

    // Helper handling the data from the network call and sending it for further processing
    private fun newDataReceivedHelper(result: MailboxStatus) {
        // Handle status update
        if (result.curStatus.timestamp != null) {
            Timber.d("Received status: ${result.curStatus}")
            MailboxApp.setStatus(result.curStatus)
        } else Timber.d("No mail has yet been received!")

        // Handle log update
        if (result.logs.isNotEmpty()) {
            Timber.d("Received logs: ${result.logs}")
            MailboxApp.setPostEntries(result.logs)
        } else Timber.d("No logs exist!")

    }

    // Exponential backoff function
    suspend fun <T> retryIO(
        times: Int = 7,
        initialDelay: Long = 1000, // 1 second
        maxDelay: Long = 1000 * 60 * 30,    // 30 minutes
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: IOException) {
                // you can log an error here and/or make a more finer-grained
                // analysis of the cause to see if retry is needed
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }

    fun doNetworkRequest(type: String, timestamp: String?, id: Int?, newMail: Boolean?) {
        val instance: MailboxApp = MailboxApp.getInstance()
        MailboxApp.getAppScope().launch {

            when (type) {
                
                // Send the log off to api after picking up the mail
                instance.getString(R.string.sendLogsMethod) -> {
                    if (timestamp == null) {
                        Timber.d("Timestamp is null when trying to add new post log entry")
                        throw NullPointerException()
                    }
                    apiInterfaceMailNotifications.setPostPickedUp(timestamp)
                }

                // Delete a log entry
                instance.getString(R.string.deleteLogsMethod) -> {
                    if (id == null) {
                        Timber.d("Id is null when trying to delete log entry")
                        throw NullPointerException()
                    }
                    apiInterfaceMailNotifications.deleteMailboxStatusById(id)
                }

                // Look for new post submitted by others (status)
                instance.getString(R.string.get_last_status_update_method) -> {
                    apiInterfaceMailNotifications.getLastMailboxStatus()
                }

                // New communication with BT device / new mail pickup - share with online api
                instance.getString(R.string.set_last_status_update_method) -> {
                    if (newMail == null) {
                        Timber.d("newMail is null when trying to set new status update")
                        throw NullPointerException()
                    }

                    val postUpdateStatus: PostUpdateStatus = PostUpdateStatus(newMail, timestamp = getTime(), username = MailboxApp.getUsername())
                    apiInterfaceMailNotifications.setLastMailboxStatus(postUpdateStatus)

                }

                // Get update from server (logs)
                instance.getString(R.string.get_logs) -> {
                    apiInterfaceMailNotifications.getRecentMailboxStatus()
                }

                else -> throw java.lang.Exception("Unknown http method!")
            }
        }
    }

    // ----------- OLD ------------------
    /*

    // TODO remove and use new version instead
    fun startDataRenewer() {
        //Log.e("Debug", "Util initiated")
        // on init
        val context = MailboxApp.getInstance()
        myNotificationManager = MyNotificationManager(context)
        val myHandler = Handler(Looper.getMainLooper())
        MailboxApp.getAppScope().launch {
            myHandler.postDelayed(object : Runnable {
                override fun run() {
                    // Check if error when getting new data
                    if (!getAllPostNotificationFromWebHelper(null)) {
                        Timber.d("Error when getting logs from server")
                    }
                    MailboxApp.getBTConn().bleScan(ScanType.BACKGROUND)
                    val lastCheck = getAllPostNotificationFromWebHelper(updateURL)
                    // if there was new mail
                    if (!lastCheck) {
                        Timber.d("Error when checking for new status with server")
                    }

                    //1 second * 60 * 30 = 30 min
                    // 1000 * 60 * 10 = 10min between updates in prod!
                    // Use 1000 * 10 for testing (10sec)
                    myHandler.postDelayed(this, (1000 * 60 * 10).toLong())
                }
                //1 second delay before first start
            }, 1000)
        }
    }

    suspend fun doNetworkRequest(type: String, timestamp: String?, id: Int?, newMail: Boolean?): Boolean {
        return suspendCoroutine {
            val context = MailboxApp.getInstance()
            // Do async thread with network request
            Timber.d("Type: $type Timestamp: $timestamp Id: $id")


            var sent = false
            var tries = 0
            do {
                // 5 seconds
                val base = 5000.0
                // Exponentially increase wait time between tries
                val time: Double = base.pow(n = tries)

                val thread = Thread {
                    // Try to send web request
                    try {
                        Timber.d("Sleeping")
                        Thread.sleep(time.toLong())
                        when (type) {
                            // Send the log off to api after picking it up
                            context.getString(R.string.sendLogsMethod) -> {
                                if (timestamp == null) {
                                    Timber.d("Timestamp is null when trying to add new post log entry")
                                    throw NullPointerException()
                                }
                                notifyPostPickedUpByUserHelper(timestamp).also { sent = it }
                            }

                            // Delete an entry
                            context.getString(R.string.deleteLogsMethod) -> {
                                if (id == null) {
                                    Timber.d("Id is null when trying to delete log entry")
                                    throw NullPointerException()
                                }
                                delLog(id).also { sent = it }
                            }

                            // Look for new post submitted by others (status)
                            context.getString(R.string.get_last_status_update_method) -> {
                                getAllPostNotificationFromWebHelper(updateURL).also { sent = it }
                            }

                            // New communication with BT device / new mail pickup - share with online api
                            context.getString(R.string.set_last_status_update_method) -> {
                                if (newMail == null) {
                                    Timber.d("newMail is null when trying to set new status update")
                                    throw NullPointerException()
                                }
                                sendUpdateFromBTSensorToWebHelper(
                                    PostUpdateStatus(
                                        newMail,
                                        timestamp = getTime(),
                                        MailboxApp.getUsername()
                                    )
                                ).also { sent = it }
                            }

                            // Get update from server (logs)
                            context.getString(R.string.get_logs) -> {
                                getAllPostNotificationFromWebHelper(null).also { sent = it }
                            }

                            else -> throw java.lang.Exception("Unknown http method!")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Timber.d("Trying transmission $tries / 6")
                thread.start()
                thread.join()
                tries++

                // Check if we succeeded or if we are giving up
                if (tries >= 7 || sent) {
                    if (tries >= 7) {
                        Timber.d("Tried 6 transmissions but failed - Giving up! ")
                        Toast.makeText(
                            MailboxApp.getInstance().applicationContext,
                            "Failed to save timestamp! Giving up!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Timber.d("Transmission success for type: $type!")
                    }
                    break
                }
            } while (!sent)
        }
    }

    // Set last time we got info from BT Device
    fun sendUpdateFromBTSensorToWebHelper(data: PostUpdateStatus): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            try {
                httpRequests.sendUpdateFromBTSensorToWeb(data).also {
                    tmpRes = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Timber.d(res.toString())
        // Update with latest info from server
        if (!getAllPostNotificationFromWebHelper(updateURL)) Timber.d("Error when getting status from server!")

        return res
    }

    // Send log to server, telling it we picked up the mail
    private fun notifyPostPickedUpByUserHelper(timestamp: String): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            try {
                httpRequests.notifyPostPickedUpByUser(timestamp).also {
                    tmpRes = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Timber.d(res.toString())
        // Update with latest info from server
        if (!getAllPostNotificationFromWebHelper(updateURL)) Timber.d("Error when getting status from server!")
        return res
    }

    // Get data from API, can be Logs or Update
    fun getAllPostNotificationFromWebHelper(myUrl: URL?): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = ""
            val thread = Thread {
                // Try to send web request to base url
                try {
                    httpRequests.getAllPostNotificationFromWeb(myUrl).also {
                        tmpRes = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
            return@runBlocking tmpRes
        }
        val gotValidResult = res != ""
        Timber.d("Valid result? $gotValidResult")


        val mapper = jacksonObjectMapper()

        // Getting Update-data since we specified an URL
        if (myUrl != null && gotValidResult) {
            // Convert data to ArrayList using jackson
            if (res.contains("\"timestamp\":\"\"")) {
                Timber.d("Data not yet initialized in server!")
            } else {
                val dataParsed: PostUpdateStatus = mapper.readValue(res)
                Timber.d(dataParsed.toString())
                // Call to store value
                updateStatus(dataParsed)
            }
        } else if (gotValidResult) {
            // Getting Log-data since we didn't specify an URL
            // Convert data to ArrayList using jackson
            val dataParsed: MutableList<PostLogEntry> = mapper.readValue(res)
            Timber.d(dataParsed.toString())
            // Call to store value
            updateLogs(dataParsed)
        }

        // return true if we received data
        return gotValidResult
    }

    // Delete a log from the list
    private fun delLog(id: Int): Boolean {
        val res = runBlocking {
            // Create thread
            var tmpRes = false
            // Try to send web request
            try {
                httpRequests.deleteLog(id).also { tmpRes = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@runBlocking tmpRes
        }
        Timber.d(res.toString())
        // Fetch new data from web
        if (!getAllPostNotificationFromWebHelper(null)) Timber.d("Error when getting new logs from server!")
        return res
    }

    // Helper function to update status from result of HTTP call
    private fun updateStatus(data: PostUpdateStatus) {
        MailboxApp.setStatus(data)
    }

    // Helper function to update logs from result of HTTP call
    private fun updateLogs(data: MutableList<PostLogEntry>) {
        MailboxApp.setPostEntries(data)
    }


     */
    // ----------- END OLD ------------------

    // ----------------------------- BT -------------------------------
    fun btEnabled() {
        Timber.d("Proxied from Util")

        MailboxApp.getBTConn().btEnabledConfirmed()
    }

    // ----------------------------- DIV -------------------------------

    fun getMyDate(str: String): String {
        if (str == "") {
            Timber.e("In string is empty!")
            throw java.lang.NullPointerException("Input string is empty")
        }
        return str.split("T")[0]
    }

    fun getMyTime(str: String): String {
        if (str == "") {
            Timber.e("In string is empty!")
            throw java.lang.NullPointerException("Input string is empty")
        }
        return str.split("T")[1].subSequence(0, 8).toString()
    }

    fun logButtonPress(msg: String) {
        Timber.d(msg)
    }

    fun getTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().toString()
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-ddTHHmmssZ", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())
            Timber.d(currentDateAndTime)
            currentDateAndTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    // Activate alarm
    fun activateAlarm(hourTime: Int, minuteTime: Int) {

        val hour = if (hourTime == -1) 21 else hourTime
        val minute = if (minuteTime == -1) 0 else minuteTime

        Timber.d("Received values: $hour:$minute")

        val context: Context? = MailboxApp.getContext()

        alarmManager =
            (context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)!!


        // Current time
        val timeNow = Calendar.getInstance()

        // When the actual alarm is supposed to go off
        val alertTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)

            Timber.d("Cur: " + timeNow.timeInMillis + " - alertTime: " + this.timeInMillis)
            // If it is passed the trueAlertTime, add a day
            if (timeNow.timeInMillis >= this.timeInMillis) {
                Timber.d("Time passed - increasing day by 1")
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val alarmIntent = Intent(context, RepeatedTrigger::class.java)
            .putExtra("hour", hour)
            .putExtra("minute", minute)
            .putExtra("alertTime", alertTime.timeInMillis)
        alarmIntent.action = "AlarmAction"

        Timber.d(
            "Intent values: " + alarmIntent.getIntExtra(
                "hour",
                -1
            ) + ":" + alarmIntent.getIntExtra(
                "minute",
                -1
            ) + " - " + alarmIntent.getLongExtra("alertTime", -1)
        )
        alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        // Change to every hour, then check if correct hour in RepeatedTrigger?
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            alertTime.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
            alarmPendingIntent
        )
    }


    fun cancelAlarm() {
        Timber.d("Trying to cancel alarm")
        try {
            // If the alarm has been set, cancel it.
            alarmManager.cancel(alarmPendingIntent)
        } catch (e: UninitializedPropertyAccessException) {
            Timber.d("This is fine - AlertManager has not yet been initialized!")
        } catch (e: java.lang.Exception) {
            Timber.d(e.stackTraceToString())
            Timber.d("PendingIntent likely not set. WHat type of error??")
        }
    }

    fun userCheck(id: Int?): User? {
        return if (id != null) {
            if (id == user?.id) user else null
        } else null
    }

    suspend fun signup(user: User): User {
        Timber.d("Signup - Arrived")

        return apiInterfaceUser.signup(user)
    }

    suspend fun login(user: User): User {
        Timber.d("Login - Arrived")

        return apiInterfaceUser.login(user)
    }

    suspend fun getUser(id: Int): User {
        return apiInterfaceUser.getUser(id)
    }

    suspend fun getUsers(): User {
        return apiInterfaceUser.getUsers()
    }

    suspend fun updateUser(user: User): User {
        return apiInterfaceUser.updateUser(user.id!!, user)
    }

    suspend fun deleteUser(id: Int): ConcreteGenericType {
        return apiInterfaceUser.deleteUser(id)
    }

    fun moveToLoginFragment(name: String, frag: Fragment) {
        Timber.d("Not logged in, so moving from $name fragment to loginFragment")
        val navcontroller = NavHostFragment.findNavController(frag)
        if (name == "alert") navcontroller.navigate(AlertFragmentDirections.actionAlertFragmentToLoginFragment())
        if (name == "debug") navcontroller.navigate(DebugFragmentDirections.actionDebugFragmentToLoginFragment())
        if (name == "postView") navcontroller.navigate(PostViewFragmentDirections.actionLogviewFragmentToLoginFragment())
        if (name == "pillLog") navcontroller.navigate(PillLogFragmentDirections.actionPillLogFragmentToLoginFragment())
        if (name == "pill") navcontroller.navigate(PillFragmentDirections.actionPillFragmentToLoginFragment())
        if (name == "pillUpdate") navcontroller.navigate(PillUpdateFragmentDirections.actionPillUpdateFragmentToLoginFragment())
        else Timber.e("Name $name not found - cannot redirect to login fragment")
    }

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("Received error: " + exception.message + "!")
        Timber.e("Trace:", exception.printStackTrace())
        Toast.makeText(
            MailboxApp.getContext(),
            "Failed to fetch data!",
            Toast.LENGTH_LONG
        ).show()
    }

    fun fetchRepoData(callback: () -> Unit) {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val prefs = MailboxApp.getPrefs()

            val token = prefs.getString("Token", "")
            Timber.d("Token from sharedPrefs: $token")

            if (token != "" && token != null) authInterceptor.Token(token)
            else Timber.e("Something went very wrong - user has no Token: $token")

            if (user == null) user = getUsers()
            Timber.d("User: $user")
            // fetch data in the background
            dayrepo.getDays()
            Timber.d("Get all days finished")
            pillrepo.getPills()
            Timber.d("Get all pills finished")
            recordrepo.getRecords()
            Timber.d("Repository sizes - Pills: ${pillrepo.data.value?.size ?: 0} - Records: ${recordrepo.data.value?.size ?: 0}")
            callback.invoke()
        }
    }


    fun fetchRepoData() {
        // setup coroutine
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            // fetch data in the background
            dayrepo.getDays()
            Timber.d("Get all days finished")
            pillrepo.getPills()
            Timber.d("Get all pills finished")
            recordrepo.getRecords()
            Timber.d("Repository sizes - Pills: ${pillrepo.data.value?.size ?: 0} - Records: ${recordrepo.data.value?.size ?: 0}")
        }
    }

    fun today(): String {
        return getTime().split("T")[0]
    }
}