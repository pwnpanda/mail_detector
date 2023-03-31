package com.robinlunde.mailbox

//import android.os.Handler
//import android.os.Looper
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import kotlin.coroutines.suspendCoroutine
//import kotlin.math.pow
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.robinlunde.mailbox.alert.AlertFragmentDirections
import com.robinlunde.mailbox.datamodel.MyMessage
import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import com.robinlunde.mailbox.datamodel.pickupStatus
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    private var httpRequestLib2 = HttpRequestLib2.getClient(this)

    private var apiInterfaceUser: ApiInterfaceUser =
        httpRequestLib2.create(ApiInterfaceUser::class.java)
    lateinit var authInterceptor: AuthenticationInterceptor

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

    private data class MailboxStatus(
        val curStatus: PostUpdateStatus,
        val logs: MutableList<PostLogEntry>
    )

    // Helper-flow for running task periodically
    // https://kotlinlang.org/docs/flow.html#flow-cancellation-basics
    // https://stackoverflow.com/questions/54827455/how-to-implement-timer-with-kotlin-coroutines
    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        var run = -1;
        while (true) {
            MailboxApp.getBTConn().bleScan(ScanType.BACKGROUND)
            run++;
            // Run once every 10 times, which is every 5 minutes.
            if (run == 0 || run >= 10) {
                CoroutineScope(Dispatchers.IO).async {
                    Timber.d("Fetching logs periodically and on startup!")
                    val getLogs =
                        apiInterfaceMailNotifications.getRecentMailboxStatus().execute()
                            .body()!! // getAllPostNotificationFromWebHelper(null)
                    MailboxApp.setPostEntries(getLogs)
                    Timber.d("getLogs done")
                    val getStatus =
                        apiInterfaceMailNotifications.getLastMailboxStatus().execute()
                            .body()!! //getAllPostNotificationFromWebHelper(updateURL)
                    MailboxApp.setStatus(getStatus)
                    Timber.d("getStatus done")
                    val myMailboxStatus = MailboxStatus(getStatus, getLogs)
                    Timber.d("MailboxStatus done")
                    emit(myMailboxStatus)
                }
                run = 0;
            }
        delay(period)
        }
    }

    fun startDataRenewer() {
        val appScope = MailboxApp.getAppScope()
        appScope.launch {
            // Run every 30 seconds, and wait 1 second before starting the first time
            tickerFlow((30).seconds, 1.seconds).collect { result ->
                newDataReceivedHelper(result)
            }
        }
    }

    // Helper handling the data from the network call and sending it for further processing
    private fun newDataReceivedHelper(result: MailboxStatus) {
        // Handle status update
        if (result.curStatus.timestamp != "") {
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
    private suspend fun <T> retryIO(
        times: Int = 7,
        initialDelay: Long = 5000, // 1 second
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
                Timber.d("Request try number ${7-times} for request")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }

    // https://www.valueof.io/blog/kotlin-flow-retry-policy-with-exponential-backoff ?
    fun doNetworkRequest(type: String, timestamp: String?, id: Int?, newMail: Boolean?) =
        CoroutineScope(Dispatchers.IO).async {
            val instance: MailboxApp = MailboxApp.getInstance()

            when (type) {

                // Send the log off to api after picking up the mail
                instance.getString(R.string.sendLogsMethod) -> {
                    if (timestamp == null) {
                        Timber.d("Timestamp is null when trying to add new post log entry")
                        throw NullPointerException()
                    }
                    return@async retryIO {
                        val req = apiInterfaceMailNotifications.setPostPickedUp(
                            pickupStatus(
                                delivered = timestamp,
                                username = MailboxApp.getUsername(),
                                pickup = getTime()
                            )
                        ).execute()
                        if (req.isSuccessful) {
                            val req2 = apiInterfaceMailNotifications.getRecentMailboxStatus().execute()
                            if (req2.isSuccessful) MailboxApp.setPostEntries( req2.body()!! )
                        }
                        return@retryIO req.body()!!
                    }
                }

                // Delete a log entry
                instance.getString(R.string.deleteLogsMethod) -> {
                    if (id == null) {
                        Timber.d("Id is null when trying to delete log entry")
                        throw NullPointerException()
                    }
                    return@async retryIO {
                        val req = apiInterfaceMailNotifications.deleteMailboxStatusById(id).execute()
                        if (req.isSuccessful){
                            val req2 = apiInterfaceMailNotifications.getRecentMailboxStatus().execute()
                            if (req2.isSuccessful) MailboxApp.setPostEntries( req2.body()!! )
                        }
                        return@retryIO req
                    }
                }

                // Look for new post submitted by others (status)
                instance.getString(R.string.get_last_status_update_method) -> {
                    return@async retryIO {
                        val req = apiInterfaceMailNotifications.getLastMailboxStatus().execute()
                        if (req.isSuccessful) MailboxApp.setStatus(req.body()!!)
                        return@retryIO req.body()
                    }
                }

                // New communication with BT device / new mail pickup - share with online api
                instance.getString(R.string.set_last_status_update_method) -> {
                    if (newMail == null) {
                        Timber.d("newMail is null when trying to set new status update")
                        throw NullPointerException()
                    }

                    val postUpdateStatus = PostUpdateStatus(
                        newMail,
                        timestamp = timestamp ?: getTime(),
                        username = MailboxApp.getUsername()
                    )
                    return@async retryIO {
                        val status =  apiInterfaceMailNotifications.setLastMailboxStatus(
                            postUpdateStatus
                        ).execute().body()
                        if (status!!.success) MailboxApp.setStatus(postUpdateStatus)
                        return@retryIO status
                    }
                }

                // Get update from server (logs)
                instance.getString(R.string.get_logs) -> {
                    return@async retryIO {
                        val logs = apiInterfaceMailNotifications.getRecentMailboxStatus().execute()
                        if (logs.isSuccessful) MailboxApp.setPostEntries(logs.body()!!)
                        return@retryIO logs.body()
                    }

                }

                else -> throw java.lang.Exception("Unknown http method!")
            }
        }

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