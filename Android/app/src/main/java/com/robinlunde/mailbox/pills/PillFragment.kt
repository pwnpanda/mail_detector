package com.robinlunde.mailbox.pills

import android.os.Bundle
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

    // Need to store alarm time in sharedPreferences

    // Need to set alarm time to correct time when window loads (from sharedPreferences)

    // https://stackoverflow.com/a/34917457
    // Change color and aspect of single drawable instance
    // Needed for date circles and for "pill taken" circles

    // Create circle as drawable resource
    // https://stackoverflow.com/a/24682125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Update UI if new data!

        /** Possible updates:
         *   New pill is added
         *   Pill is taken
         *   Pill is disabled
         *   Alarm is changed and/or set
         *   Date changes
        **/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill,
            container,
            false
        )

        // Set 24 hour display
        binding.setAlarm.setIs24HourView(true);

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val util: Util = MailboxApp.getUtil()
        return when (item.itemId) {
            R.id.logo -> {
                util.logButtonPress("Pill - logo")
                NavHostFragment.findNavController(this).navigate(PillFragmentDirections.actionPillFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("Pill - logs")
                NavHostFragment.findNavController(this).navigate(PillFragmentDirections.actionPillFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("Pill - bt")
                NavHostFragment.findNavController(this).navigate(PillFragmentDirections.actionPillFragmentToDebugFragment())
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
}
