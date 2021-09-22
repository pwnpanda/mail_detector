package com.robinlunde.mailbox.pills

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentPillBinding
import kotlinx.coroutines.*
import java.util.*


class PillFragment : Fragment() {
    private lateinit var binding: FragmentPillBinding
    private lateinit var dayCircleObjects: Array<Button>
    private lateinit var dayHeaderObjects: Array<TextView>
    private lateinit var dayPillTakenObjects: Array<LinearLayoutCompat>
    private lateinit var todayObject: Calendar
    private var curWeekDates = IntArray(7)
    private var todayIndex: Int = -1
    private var todayDate: Int = -1
    private val prefs: SharedPreferences = MailboxApp.getPrefs()
    private val util: Util = MailboxApp.getUtil()
    private val timer: Timer = Timer()
    val logTag = "PillFragment -"

    // setup coroutine
    private val mainActivityJob = Job()
    val coroutineScope = CoroutineScope(mainActivityJob + Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Update UI if new data!

        /** Todo: Possible UI updates:
         *   New pill is added
         *   Pill is taken
         *   Pill is disabled
         *   Alarm is changed and/or set
         *   Date changes - If date changes, check if it is in dates. If it is, update today and call updateWeekView()
         **/

        /** Todo: Check if alarm should be disabled when:
         *   Pill is disabled
         *   Pill is taken
         **/

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill,
            container,
            false
        )

        // If not logged in, jump to login view
        if (util.user == null) util.moveToLoginFragment("pill", this)

        setAlarmIfConfigured()

        // Set 24 hour display
        binding.setAlarm.setIs24HourView(true)

        binding.lifecycleOwner = viewLifecycleOwner

        // Handle alarm being set
        binding.alarmButton.setOnClickListener { handleAlarm() }
        // Handle alarm being cancelled
        binding.cancelAlarmButton.setOnClickListener { util.cancelAlarm() }
        // Handle registration of new pill
        binding.pillButtonLayoutIncl.pillRegisterButton.setOnClickListener { registerPillTakenButton() }
        // Handle creation of new pill
        binding.pillButtonLayoutIncl.pillCreateButton.setOnClickListener { createPill() }
        // Handle invalidating existing pill
        binding.pillButtonLayoutIncl.pillDisableButton.setOnClickListener { handleDisablePill() }
        // Handle pill history button
        binding.pillButtonLayoutIncl.pillHistoryButton.setOnClickListener { pillHistory() }

        updateWeekView()

        return binding.root
    }

    override fun onDestroy() {
        // Cancel timer if we move out of fragment
        timer.cancel()
        super.onDestroy()
    }

    private fun registerPillTakenButton() {
        // Show current pills
        // Clicking one sends relevant API request to register it as taken
        // Update alarm-setting logic (pill is taken, so cancel alarm if all are taken)

        // Hide buttons
        //binding.pillButtonLayoutIncl.root.visibility = View.GONE
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.GONE
        // Show input
        binding.pillTakenLayoutIncl.pillTakenLayout.visibility = View.VISIBLE
        //binding.pillTakenLayoutIncl.root.visibility = View.VISIBLE

        // Create click listener
        binding.pillTakenLayoutIncl.button.setOnClickListener { registerPillTakenAction() }
    }

    private fun registerPillTakenAction() {

        // TODO actually do operations

        // show Buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE

        // Hide input
        binding.pillTakenLayoutIncl.pillTakenLayout.visibility = View.INVISIBLE

    }

    private fun createPill() {
        // Show UI for creating pill

        val button = binding.pillCreateLayoutIncl.createPillColor
        var color = resources.getColor(R.color.button, null)

        // Hide buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.INVISIBLE
        // Show create
        binding.pillCreateLayoutIncl.pillCreateLayout.visibility = View.VISIBLE

        // Create click listener for picking color - set color!
        button.setOnClickListener {
            // TODO https://github.com/QuadFlask/colorpicker
            // Need dialog for picking from set of colors or picking individual color
            // Then show correct view - maybe alert dialog or similar?
            // TODO IDEA: Show custom view with 2 "buttons" (nicely designed selectors on top) for each type and the actual view underneath?
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    color = selectedColor
                    Log.d("$logTag ColorSelector", "Temporary selected color $selectedColor")
                }
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    color = selectedColor
                    button.setBackgroundColor(selectedColor)
                    Log.d("$logTag ColorSelector", "Selected color $selectedColor")
                }
                .setNegativeButton("cancel") { dialog, which -> }
                .build()
                .show()
        }

        val active = binding.pillCreateLayoutIncl.activePill.isChecked

        // Create click listener for creating a pill
        binding.pillCreateLayoutIncl.createPillCreateButton.setOnClickListener {
            createPillAction(color, active)
        }

        // Create click listener for cancelling creation of pill - move back
        binding.pillCreateLayoutIncl.createPillCancelButton.setOnClickListener {
            // Show buttons
            binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE
            // Hide create pill view
            binding.pillCreateLayoutIncl.pillCreateLayout.visibility = View.INVISIBLE
        }
    }

    private fun createPillAction(color: Int, active: Boolean) {

        // Require necessary information filled in

        // Co-routine handling
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            Log.d("$logTag createPillAction", "Received error: ${exception.message}!")
            Log.e("$logTag createPillAction", "Trace: ${exception.printStackTrace()}!")
            Toast.makeText(
                MailboxApp.getContext(),
                "Failed to fetch data!",
                Toast.LENGTH_LONG
            ).show()
        }
        coroutineScope.launch(errorHandler) {
            // Send API request
            // TODO ERROR - Cannot create RequestBody from pill!
            val pill = util.pillrepo.createPill(color, active)

            // Store name & pillId pair in secureSharedPreferences
            val name = binding.pillCreateLayoutIncl.createPillNameInput.text
            if (name.toString() == "") {
                Log.d("$logTag createPillAction", "Name is nulL! Please fill in a name!")
                Toast.makeText(
                    MailboxApp.getContext(),
                    "A name is required. Please fill in a name!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val prefs = MailboxApp.getPrefs()
                with(prefs.edit()) {
                    putString(pill.id.toString(), name.toString())
                }
                // Update alarm-setting logic (creating a pill assumes it is taken that day)
                // TODO
            }
        }

        // --- Restore view ---
        // show Buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE
        // Hide input
        binding.pillCreateLayoutIncl.pillCreateLayout.visibility = View.INVISIBLE
    }

    private fun handleDisablePill() {
        // Show all pills
        // Have checkbox for active or inactive
        // Send API request to activate / deactivate pill if clicked
        // Update circle-ui to reflect current status
        // Update alarm-setting logic (more / less pills needed for all to be taken)

        // Hide buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.INVISIBLE
        // Show input
        binding.pillDisableLayoutIncl.pillDisableLayout.visibility = View.VISIBLE

        // Create click listener
        binding.pillDisableLayoutIncl.button.setOnClickListener { disablePillAction() }
    }

    private fun disablePillAction() {

        // TODO actually do operations


        // show Buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE

        // Hide input
        binding.pillDisableLayoutIncl.pillDisableLayout.visibility = View.INVISIBLE
    }

    private fun pillHistory() {
        // TODO
        // Fetch all all history async
        // This is also needed for the week, but can be done using different API to only get within the last week

        // Go to own fragment
        NavHostFragment.findNavController(this)
            .navigate(PillFragmentDirections.actionPillFragmentToPillLogFragment())
    }

    // Set background color for any given button
    private fun setBackgroundColor(btn: View, @ColorRes color: Int) {
        btn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), color)
    }

    // Check if all pills are taken for the given date
    private fun hasTakenAllPills(date: String): Boolean {
        // todo
        //return dayData[date.toInt()].allTaken
        return true
    }

    // Show only taken pills
    private fun handlePillsTaken(buttonList: LinearLayoutCompat) {
        /*
        val colors = dayData[date.toInt()].getTakenColors
        colors.zip(buttonList.children).forEach { (color, child) -> {
            if (!color) child.visibility = View.Gone
            setBackgroundColor(child, color)
            }
        }
        */
    }

    // Set color for if all pills are taken for a given day
    private fun setIsTakenColor(obj: Button) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        // Set background color
        drawable.setColor(requireContext().getColor(R.color.background))

        if (hasTakenAllPills(obj.text.toString())) {
            // Set border
            drawable.setStroke(8, requireContext().getColor(R.color.green_pill))
            // Set text color
            obj.setTextColor(requireContext().getColor(R.color.charcoal_light))
        } else {
            //Set border
            drawable.setStroke(8, requireContext().getColor(R.color.top))
            // Set text color
            obj.setTextColor(requireContext().getColor(R.color.charcoal_light))
        }
        obj.background = drawable
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dayUIUpdate() {
        Log.d("$logTag dayUIUpdate", "Today $todayDate All Dates ${curWeekDates.joinToString(",")}")
        // TODO change - just for testing
        // todayDate = 4
        for (i in 0..6) {

            // Today
            if (curWeekDates[i] == todayDate) {
                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.OVAL
                // Set border
                drawable.setStroke(8, requireContext().getColor(R.color.charcoal_light))
                // Set background color
                drawable.setColor(requireContext().getColor(R.color.highlight))
                dayCircleObjects[i].background = drawable
                // Set bold font
                dayCircleObjects[i].text = Html.fromHtml("<b>${dayCircleObjects[i].text}</b>", 0)
                // Set underline on header
                dayHeaderObjects[i].text = Html.fromHtml("<u>${dayHeaderObjects[i].text}</u>", 0)
                // Set color black for current day in header
                dayHeaderObjects[i].setTextColor(Color.BLACK)
                handlePillsTaken(dayPillTakenObjects[i])
            }

            // Day has passed
            if (curWeekDates[i] < todayDate || todayDate < 7 && curWeekDates[i] > 29) {
                setIsTakenColor(dayCircleObjects[i])
                handlePillsTaken(dayPillTakenObjects[i])
                // Day is in the future
            } else if (curWeekDates[i] > todayDate) {
                setBackgroundColor(dayCircleObjects[i], R.color.grey)
                dayCircleObjects[i].setTextColor(requireContext().getColor(R.color.background))
                dayPillTakenObjects[i].visibility = View.GONE
            }
        }
    }

    private fun detectNewDay() {
        val tomorrowObject =
            (todayObject.clone() as Calendar).apply {
                add(Calendar.DATE, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        timer.schedule(
            object : TimerTask() {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun run() {
                    // If tomorrow is this week, just log it
                    if (tomorrowObject.get(Calendar.DATE) in curWeekDates) Log.d(
                        "$logTag detectNewDay",
                        "Date ${tomorrowObject.get(Calendar.DATE)} is this week (in ${curWeekDates.joinToString { "," }})"
                    )
                    // If not, update the WeekView
                    else updateWeekView()

                    // Set new today values
                    todayObject = Calendar.getInstance()
                    todayIndex += 1
                }
            },
            tomorrowObject.time
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateWeekView() {

        dayCircleObjects = arrayOf(
            binding.dateCircleMon,
            binding.dateCircleTue,
            binding.dateCircleWed,
            binding.dateCircleThu,
            binding.dateCircleFri,
            binding.dateCircleSat,
            binding.dateCircleSun
        )

        dayHeaderObjects = arrayOf(
            binding.monday,
            binding.tuesday,
            binding.wednesday,
            binding.thursday,
            binding.friday,
            binding.saturday,
            binding.sunday
        )

        dayPillTakenObjects = arrayOf(
            binding.pillsTakenMon,
            binding.pillsTakenTue,
            binding.pillsTakenWed,
            binding.pillsTakenThu,
            binding.pillsTakenFri,
            binding.pillsTakenSat,
            binding.pillsTakenSun
        )

        todayObject = Calendar.getInstance()
        Log.d(
            "$logTag updateWeekView",
            "Weekday ${todayObject.get(Calendar.DAY_OF_WEEK)} Date ${todayObject.get(Calendar.DAY_OF_MONTH)}"
        )

        todayDate = todayObject.get(Calendar.DAY_OF_MONTH)
        val testDate: Calendar = todayObject.clone() as Calendar

        // Get index of today's date and the date for monday this week
        val dateMonday: Int = when (todayObject.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> {
                testDate.add(Calendar.DATE, -6)
                curWeekDates[6] = testDate.get(Calendar.DAY_OF_MONTH)
                todayIndex = 6
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.MONDAY -> {
                todayIndex = 0
                todayDate
            }

            Calendar.TUESDAY -> {
                testDate.add(Calendar.DATE, -1)
                curWeekDates[1] = testDate.get(Calendar.DAY_OF_MONTH)
                todayIndex = 1
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.WEDNESDAY -> {
                testDate.add(Calendar.DATE, -2)
                curWeekDates[2] = testDate.get(Calendar.DAY_OF_MONTH)
                todayIndex = 2
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.THURSDAY -> {
                testDate.add(Calendar.DATE, -3)
                curWeekDates[3] = testDate.get(Calendar.DAY_OF_MONTH)
                todayIndex = 3
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.FRIDAY -> {
                testDate.add(Calendar.DATE, -4)
                curWeekDates[4] = testDate.get(Calendar.DAY_OF_MONTH)
                todayIndex = 4
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.SATURDAY -> {
                testDate.add(Calendar.DATE, -5)
                todayIndex = 5
                curWeekDates[5] = testDate.get(Calendar.DAY_OF_MONTH)
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            else -> -1
        }
        // Fill correct dates
        curWeekDates[0] = dateMonday
        dayCircleObjects[0].text = curWeekDates[0].toString()
        for (i in 1..6) {
            // Get current date
            val tmpCurDate = (todayObject.clone() as Calendar).apply {
                set(Calendar.DATE, dateMonday)
                add(Calendar.DATE, i)
            }
                .get(Calendar.DAY_OF_MONTH)

            // Store date in correct place
            curWeekDates[i] = tmpCurDate
            // Fill information in correct circle
            dayCircleObjects[i].text = tmpCurDate.toString()
        }

        Log.d("$logTag updateWeekView", "curDate $todayDate mondayDate $dateMonday ")

        // Organize days and color correctly
        dayUIUpdate()

        // Detect if there's a new day
        detectNewDay()
    }

    private fun handleAlarm() {
        val alarmHour = binding.setAlarm.hour
        val alarmMinute = binding.setAlarm.minute

        Log.d("PillFragment - handleAlarm", "Pressed! $alarmHour:$alarmMinute")

        // Store new alarm value in shared preferences
        with(prefs.edit()) {
            putInt(
                "alarm_hour",
                alarmHour
            )
            putInt(
                "alarm_minute",
                alarmMinute
            )
            apply()
        }

        // Activate the alarm for the given time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Send alarm to set and handle alarm 5 min before
            util.activateAlarm(alarmHour, alarmMinute)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.alert -> {
                util.logButtonPress("Pill - logo")
                NavHostFragment.findNavController(this)
                    .navigate(PillFragmentDirections.actionPillFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("Pill - logs")
                NavHostFragment.findNavController(this)
                    .navigate(PillFragmentDirections.actionPillFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Pill - bt")
                if (MailboxApp.getClickCounter() >= 3) NavHostFragment.findNavController(this)
                    .navigate(PillFragmentDirections.actionPillFragmentToDebugFragment())
                super.onOptionsItemSelected(item)
            }

            R.id.pill -> {
                util.logButtonPress("Pill - pill")
                // Do nothing we are in this view
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setAlarmIfConfigured() {
        val funcTag = "$logTag setAlarmIfConfigured"
        // Set the time for the alarm clock to the currently set value
        val hour = prefs.getInt("alarm_hour", -1)
        val minute = prefs.getInt("alarm_minute", -1)
        if (hour != -1 && minute != -1) {
            Log.d(funcTag, "Alarm time is set in sharedPrefs: $hour:$minute")
            binding.setAlarm.hour = hour
            binding.setAlarm.minute = minute

            // Activate the alarm for the given time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Send alarm to set
                util.activateAlarm(hour, minute)
            }
        }
    }
}
