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

    // use this and migrate to calendar? https://github.com/kizitonwose/CalendarView

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
        // Handle registration of new pill
        binding.registerPillButton.setOnClickListener { registerPillTaken() }
        // Handle creation of new pill
        binding.createPillButton.setOnClickListener { createPill() }
        // Handle invalidating existing pill
        binding.deletePillButton.setOnClickListener { handleDeletePill() }

        return binding.root
    }

    private fun registerPillTaken(){
        // Show current pills
        // Clicking one sends relevant API request to register it as taken
        // Update alarm-setting logic (pill is taken, so cancel alarm if all are taken)
    }

    private fun createPill(){
        // Show UI for creating pill
        // Require necessary information filled in
        // Send API request
        // Store name & pillId pair in secureSharedPreferences
        // Update alarm-setting logic (creating a pill assumes it is taken that day)
    }

    private fun handleDeletePill(){
        // Show all pills
        // Have checkbox for active or inactive
        // Send API request to activate / deactivate pill if clicked
        // Update circle-ui to reflect current status
        // Update alarm-setting logic (more / less pills needed for all to be taken)
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
                NavHostFragment.findNavController(this)
                    .navigate(PillFragmentDirections.actionPillFragmentToDebugFragment())
                true
            }

            R.id.pill -> {
                util.logButtonPress("Pill - pill")
                // Do nothing we are in this view
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setAlarmIfConfigured () {
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
