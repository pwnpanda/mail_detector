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
import com.robinlunde.mailbox.databinding.FragmentPillLogBinding

// use this and migrate to calendar? https://github.com/kizitonwose/CalendarView

class PillLogFragment: Fragment() {
    private lateinit var binding: FragmentPillLogBinding
    private val util: Util = MailboxApp.getUtil()
    val logTag = "PillLogFragment -"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Include top menu
        setHasOptionsMenu(true)

        // Update UI if new data!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pill_log,
            container,
            false
        )

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.alert -> {
                util.logButtonPress("PillLog - logo")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToAlertFragment())
                true
            }

            R.id.logs -> {
                util.logButtonPress("PillLog - logs")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToLogviewFragment())
                true
            }

            R.id.bluetooth -> {
                util.logButtonPress("PillLog - bt")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToDebugFragment())
                true
            }

            R.id.pill -> {
                util.logButtonPress("PillLog - pill")
                NavHostFragment.findNavController(this)
                    .navigate(PillLogFragmentDirections.actionPillLogFragmentToPillFragment())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}