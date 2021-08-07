package com.robinlunde.mailbox.pills

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.Util
import com.robinlunde.mailbox.databinding.FragmentPillBinding
import java.util.*


class PillFragment : Fragment() {
    private lateinit var binding: FragmentPillBinding
    private val prefs: SharedPreferences = MailboxApp.getPrefs()
    private val util: Util = MailboxApp.getUtil()
    val logTag = "PillFragment -"

    // https://stackoverflow.com/a/34917457
    // Change color and aspect of single drawable instance
    // Needed for date circles and for "pill taken" circles

    // Create circle as drawable resource
    // https://stackoverflow.com/a/24682125

// Use util.cancelAlarm() to cancel current alarm

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
         *   Date changes
         **/

        /** Todo: Check if alarm should be disabled when:
         *   Pill is disabled
         *   Pill is taken
         **/

        // Todo Activate alarm if new pill is added
    }

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

        updateWeekView(binding)

        return binding.root
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
        // Require necessary information filled in
        // Send API request
        // Store name & pillId pair in secureSharedPreferences
        // Update alarm-setting logic (creating a pill assumes it is taken that day)

        // Hide buttons
        binding.pillButtonLayoutIncl.pillButtonLayout.visibility = View.INVISIBLE
        // Show create
        binding.pillCreateLayoutIncl.pillCreateLayout.visibility = View.VISIBLE

        // Create click listener
        binding.pillCreateLayoutIncl.button.setOnClickListener { createPillAction() }
    }

    private fun createPillAction() {

        // TODO actually do operations


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

    fun updateWeekView(binding: FragmentPillBinding) {

        val now: Calendar = Calendar.getInstance()
        Log.d(
            "$logTag now",
            "Weekday ${now.get(Calendar.DAY_OF_WEEK)} Date ${now.get(Calendar.DAY_OF_MONTH)}"
        )

        val curDate = now.get(Calendar.DAY_OF_MONTH)
        val testDate: Calendar = now.clone() as Calendar
        val dates = IntArray(7)
        val today : Int

        val dateMonday: Int = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> {
                testDate.add(Calendar.DATE, -6)
                dates[6] = testDate.get(Calendar.DAY_OF_MONTH)
                today = 6
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.MONDAY -> {
                today = 0
                curDate
            }

            Calendar.TUESDAY -> {
                testDate.add(Calendar.DATE, -1)
                dates[1] = testDate.get(Calendar.DAY_OF_MONTH)
                today = 1
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.WEDNESDAY -> {
                testDate.add(Calendar.DATE, -2)
                dates[2] = testDate.get(Calendar.DAY_OF_MONTH)
                today = 2
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.THURSDAY -> {
                testDate.add(Calendar.DATE, -3)
                dates[3] = testDate.get(Calendar.DAY_OF_MONTH)
                today = 3
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.FRIDAY -> {
                testDate.add(Calendar.DATE, -4)
                dates[4] = testDate.get(Calendar.DAY_OF_MONTH)
                today = 4
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            Calendar.SATURDAY -> {
                testDate.add(Calendar.DATE, -5)
                today = 5
                dates[5] = testDate.get(Calendar.DAY_OF_MONTH)
                testDate.get(Calendar.DAY_OF_MONTH)
            }

            else -> -1
        }
        for (i in 1..6) {
            dates[i] = (now.clone() as Calendar).apply {
                set(Calendar.DATE, dateMonday)
                add(Calendar.DATE, i)
            }
                .get(Calendar.DAY_OF_MONTH)
        }

        /** TODO
         *  Need to find a better way to deal with dates and weekdays
         *  Create a enum and reference
         *  Need to set today style based on if date is before or after today
         *  Should have 1 enum for number to string
         *  May want an enum from number to object
         */

        Log.d("$logTag now", "curDate $curDate mondayDate $dateMonday ")
        binding.dateCircleMon.text = dateMonday.toString()
        binding.dateCircleTue.text = dates[1].toString()
        binding.dateCircleWed.text = dates[2].toString()
        binding.dateCircleThu.text = dates[3].toString()
        binding.dateCircleFri.text = dates[4].toString()
        binding.dateCircleSat.text = dates[5].toString()
        binding.dateCircleSun.text = dates[6].toString()
    }

    private fun handleAlarm() {
        val alarmHour = binding.setAlarm.hour
        val alarmMinute = binding.setAlarm.minute

        Log.d("PillFragment - AlarmButton", "Pressed! $alarmHour:$alarmMinute")

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
