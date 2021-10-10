package com.robinlunde.mailbox.pills

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentPillBinding
import com.robinlunde.mailbox.datamodel.pill.Pill
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.reflect.Method
import java.util.*


class PillFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentPillBinding
    private lateinit var dayCircleObjects: Array<Button>
    private lateinit var dayHeaderObjects: Array<TextView>
    private lateinit var dayPillTakenObjects: Array<LinearLayoutCompat>
    private lateinit var todayObject: Calendar
    private lateinit var spinner: Spinner
    private var curWeekDatesDay = IntArray(7)
    private var curWeekDates: Array<Calendar> = Array(7) { Calendar.getInstance() }
    private var todayIndex: Int = -1
    private var todayDate: Int = -1
    private val prefs: SharedPreferences = MailboxApp.getPrefs()
    private val util: Util = MailboxApp.getUtil()
    private val timer: Timer = Timer()
    private var selectedPill: Pill? = null

    // setup coroutine
    private val mainActivityJob = Job()
    val coroutineScope = CoroutineScope(mainActivityJob + Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Watch data
        val observer = Observer<MutableList<Pill>> { newData ->
            spinner = binding.pillTakenLayoutIncl.dropdown

            spinner.adapter = createAdapter(util.pillrepo.data.value!!, spinner)
            spinner.onItemSelectedListener = this

            // Create click listener
            binding.pillTakenLayoutIncl.takenButton.setOnClickListener {
                registerPillTakenAction(
                    selectedPill
                )
            }
            // Notify new data at end
            (spinner.adapter as PillTakenAdapter).notifyDataSetChanged()
            // TODO make logic for changing and inserting data. This is not good enough and is wrong, but works ad hoc
            /**
             * val curSize = binding.pillLogEntries.adapter?.itemCount!!
             * val newItems = util.pillrepo.data.value?.size!!
             * if (curSize < newItems) binding.pillLogEntries.adapter?.notifyItemRangeInserted(curSize, newItems-curSize)
             */

        }
        util.pillrepo.data.observe(this, observer)


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
        binding.pillButtonLayoutIncl.pillDisableButton.setOnClickListener { handleUpdatePill() }
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

    private fun createAdapter(pills: MutableList<Pill>, spinner: Spinner): PillTakenAdapter {
        val data = mutableListOf<Pill>()
        for (pill in pills) {
            if (!pill.active) continue
            val records = util.recordrepo.findRecordsByPill(pill)
            var taken = false
            if (records != null) {
                for (rec in records) if (rec.day!!.today == util.today()) taken = rec.taken
            }
            if (taken) continue
            data.add(pill)
        }
        // https://www.tutorialsbuzz.com/2019/09/android-kotlin-custom-spinner-image-text.html
        // TODO change from dropdown to checkbox list?
        return PillTakenAdapter(this, data, requireContext(), spinner)
    }

    private fun registerPillTakenButton() {
        // Show current pills

        // Hide buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.GONE
        // Show input
        binding.pillTakenLayoutIncl.pillTakenLayout.visibility = View.VISIBLE

        spinner = binding.pillTakenLayoutIncl.dropdown

        spinner.adapter = createAdapter(util.pillrepo.data.value!!, spinner)
        spinner.onItemSelectedListener = this

        // Create click listener
        binding.pillTakenLayoutIncl.takenButton.setOnClickListener {
            registerPillTakenAction(
                selectedPill
            )
        }

        // Go back!
        binding.pillTakenLayoutIncl.cancelButton.setOnClickListener {
            // show Buttons
            binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE

            // Hide input
            binding.pillTakenLayoutIncl.pillTakenLayout.visibility = View.INVISIBLE
        }
    }

    private fun registerPillTakenAction(pill: Pill?) {

        // Co-routine handling
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            Timber.d("Received error: ${exception.message}!")
            Timber.e("Trace: ${exception.printStackTrace()}!")
            Toast.makeText(
                MailboxApp.getContext(),
                "Failed to register pill as taken for Pill: $pill!",
                Toast.LENGTH_LONG
            ).show()
        }

        if (pill != null) {
            Timber.d("Pill $pill selected. Processing!")
            coroutineScope.launch(errorHandler) {
                val curDay = util.dayrepo.createDay(util.today())
                val record = util.recordrepo.createRecord(
                    day_id = curDay.id!!,
                    pill_id = pill.id!!,
                    taken = true
                )
                Timber.d("Created record: $record")
            }
        }

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
            //  Need dialog for picking from set of colors or picking individual color
            //  Then show correct view - maybe alert dialog or similar?
            //  IDEA: Show custom view with 2 "buttons" (nicely designed selectors on top) for each type and the actual view underneath?
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    color = selectedColor
                    Timber.d("Temporary selected color $selectedColor")
                }
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    color = selectedColor
                    button.setBackgroundColor(selectedColor)
                    Timber.d("Selected color $selectedColor")
                }
                .setNegativeButton("cancel") { dialog, which -> }
                .build()
                .show()
        }

        var active = binding.pillCreateLayoutIncl.activePill.isChecked
        Timber.d("isActive: $active")
        // Needs onclick to not just stay at initial value
        binding.pillCreateLayoutIncl.activePill.setOnClickListener {
            active = binding.pillCreateLayoutIncl.activePill.isChecked
            Timber.d("isActive: $active")
        }

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
            Timber.d("Received error: ${exception.message}!")
            Timber.e("Trace: ${exception.printStackTrace()}!")
            Toast.makeText(
                MailboxApp.getContext(),
                "Failed to fetch data!",
                Toast.LENGTH_LONG
            ).show()
        }
        coroutineScope.launch(errorHandler) {

            // Store name & pillId pair in secureSharedPreferences
            val name = binding.pillCreateLayoutIncl.createPillNameInput.text
            Timber.d("Name of pill is: $name")
            if (name.toString() == "") {
                Timber.d("Name is nulL! Please fill in a name!")
                Toast.makeText(
                    MailboxApp.getContext(),
                    "A name is required. Please fill in a name!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Send API request
                val pill = util.pillrepo.createPill(color, active)

                val prefs = MailboxApp.getPrefs()
                with(prefs.edit()) {
                    putString(pill.uuid!!.toString(), name.toString())
                    apply()
                }
                Timber.d("Stored ${pill.uuid!!}  - $name  in sharedPrefs")

                // Update alarm-setting logic (creating a pill assumes it is taken that day)
                val day = util.dayrepo.createDay(util.today())
                val record = util.recordrepo.createRecord(day.id!!, pill.id!!, taken = true)
                Timber.d("Created record for pill " + pill + " as taken for today " + day.today + "! Record: " + record)
            }
        }

        // --- Restore view ---
        // show Buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.VISIBLE
        // Hide input
        binding.pillCreateLayoutIncl.pillCreateLayout.visibility = View.INVISIBLE
    }

    private fun handleUpdatePill() {
        // Show all pills

        // Go to own fragment
        NavHostFragment.findNavController(this)
            .navigate(PillFragmentDirections.actionPillFragmentToPillUpdateFragment())

    }

    private fun pillHistory() {
        // Fetch all history async
        util.fetchRepoData()

        // Go to own fragment
        NavHostFragment.findNavController(this)
            .navigate(PillFragmentDirections.actionPillFragmentToPillLogFragment())
    }

    // Set background color for any given button
    private fun setBackgroundColor(btn: View, @ColorRes color: Int) {
        btn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), color)
    }


    // Show only taken pills
    private fun handlePillsTaken(buttonList: LinearLayoutCompat) {
        // TODO add logging
        val allButtons = buttonList.touchables
        val colors = util.recordrepo.getTakenColors(util.today())
        if (colors == null) {
            buttonList.visibility = View.INVISIBLE
            Timber.d("No colors available due to no pills taken. Hiding visibility")
            return
        } else {
            buttonList.visibility = View.VISIBLE
        }

        for ((i, button) in allButtons.withIndex()) {
            button.setBackgroundColor(colors[i])
            if (i >= colors.size) {
                button.visibility = View.GONE
                Timber.d("No more colors, Button $i is removed")
            } else {
                Timber.d("Button $i has color ${colors[i]}")
            }
        }
    }

    // Set color if all pills are taken for a given day
    private fun setIsTakenColor(obj: Button) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        // Set background color
        drawable.setColor(requireContext().getColor(R.color.background))
        val recordRepo = util.recordrepo
        val dateTimeIndex = curWeekDatesDay.indexOf(obj.text.toString().toInt())
        val dateTime = curWeekDates[dateTimeIndex]
        val date =
            "${dateTime.get(Calendar.YEAR)}-${dateTime.get(Calendar.MONTH)}-${dateTime.get(Calendar.DATE)}"
        Timber.d("Checking for day " + obj.text + " - translates to " + date)
        if (recordRepo.areAllTaken(date)) {
            // Set border
            drawable.setStroke(8, requireContext().getColor(R.color.green_pill))
        } else {
            //Set border
            drawable.setStroke(8, requireContext().getColor(R.color.top))
        }

        // Set text color
        obj.setTextColor(requireContext().getColor(R.color.charcoal_light))
        obj.background = drawable
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dayUIUpdate() {
        Timber.d("Today " + todayDate + " All Dates " + curWeekDatesDay.joinToString(","))
        for (i in 0..6) {
            // Today
            if (curWeekDatesDay[i] == todayDate) {
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
            if (curWeekDatesDay[i] < todayDate || todayDate < 7 && curWeekDatesDay[i] > 29) {
                setIsTakenColor(dayCircleObjects[i])
                handlePillsTaken(dayPillTakenObjects[i])
                // Day is in the future
            } else if (curWeekDatesDay[i] > todayDate) {
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
                    if (tomorrowObject.get(Calendar.DATE) in curWeekDatesDay) Timber.d(
                        "Date ${tomorrowObject.get(Calendar.DATE)} is this week (in ${curWeekDatesDay.joinToString { "," }})"
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
        Timber.d(
            "Weekday " + todayObject.get(Calendar.DAY_OF_WEEK) + " Date " + todayObject.get(
                Calendar.DAY_OF_MONTH
            )
        )

        todayDate = todayObject.get(Calendar.DAY_OF_MONTH)
        val testDate: Calendar = todayObject.clone() as Calendar

        // Get index of today's date and the date for monday this week
        val dateMonday: Int = when (todayObject.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> {
                testDate.add(Calendar.DATE, -6)
                curWeekDatesDay[6] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[6] = testDate
                todayIndex = 6
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.MONDAY -> {
                todayIndex = 0
                curWeekDates[0] = todayObject
                todayDate
            }

            Calendar.TUESDAY -> {
                testDate.add(Calendar.DATE, -1)
                curWeekDatesDay[1] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[1] = testDate
                todayIndex = 1
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.WEDNESDAY -> {
                testDate.add(Calendar.DATE, -2)
                curWeekDatesDay[2] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[2] = testDate
                todayIndex = 2
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.THURSDAY -> {
                testDate.add(Calendar.DATE, -3)
                curWeekDatesDay[3] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[3] = testDate
                todayIndex = 3
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.FRIDAY -> {
                testDate.add(Calendar.DATE, -4)
                curWeekDatesDay[4] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[4] = testDate
                todayIndex = 4
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.SATURDAY -> {
                testDate.add(Calendar.DATE, -5)
                curWeekDatesDay[5] = testDate.get(Calendar.DAY_OF_MONTH)
                curWeekDates[5] = testDate
                todayIndex = 5
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            else -> -1
        }
        // Fill correct dates
        curWeekDates[0] = testDate
        curWeekDatesDay[0] = dateMonday
        dayCircleObjects[0].text = curWeekDatesDay[0].toString()
        for (i in 1..6) {
            // Get current date
            val tmpCurDate = (curWeekDates[0].clone() as Calendar).apply {
                add(Calendar.DATE, i)
            }
            //Log.d("$logTag updateWeekView", "$tmpCurDate")
            val tmpCurDateInt = tmpCurDate.get(Calendar.DAY_OF_MONTH)
            // Store date in correct place
            curWeekDatesDay[i] = tmpCurDateInt
            // Store date object in array
            curWeekDates[i] = tmpCurDate
            // Fill information in correct circle
            dayCircleObjects[i].text = tmpCurDateInt.toString()
        }

        Timber.d("curDate $todayDate mondayDate $dateMonday ")
        // TODO test
        Timber.d(
            "curWeekDates: ${curWeekDates.map { day -> "${day.get(Calendar.DAY_OF_MONTH)}-" }}"
        )

        // Organize days and color correctly
        dayUIUpdate()

        // Detect if there's a new day
        detectNewDay()
    }

    private fun handleAlarm() {
        val alarmHour = binding.setAlarm.hour
        val alarmMinute = binding.setAlarm.minute

        Timber.d("Pressed! $alarmHour:$alarmMinute")

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
        // Set the time for the alarm clock to the currently set value
        val hour = prefs.getInt("alarm_hour", -1)
        val minute = prefs.getInt("alarm_minute", -1)
        if (hour != -1 && minute != -1) {
            Timber.d("Alarm time is set in sharedPrefs: $hour:$minute")
            binding.setAlarm.hour = hour
            binding.setAlarm.minute = minute

            // Activate the alarm for the given time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Send alarm to set
                util.activateAlarm(hour, minute)
            }
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        pos: Int,
        id: Long
    ) {
        Timber.d("Selected item: $pos")
        spinner.setSelection(pos)
        selectedPill = spinner.getItemAtPosition(pos) as Pill
        Timber.d("Selected item: $selectedPill")
        try {
            // https://stackoverflow.com/a/46906393/4400482
            val method: Method = Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
            method.isAccessible = true
            method.invoke(spinner)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Timber.d("No selected item!")
    }
}
